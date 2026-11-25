package com.mussonindustrial.ignition.embr.periscope.component

import com.inductiveautomation.ignition.common.model.ApplicationScope
import com.inductiveautomation.ignition.common.project.RuntimeProject
import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.inductiveautomation.ignition.common.project.resource.ProjectResourceId
import com.inductiveautomation.ignition.common.project.resource.ResourcePath
import com.inductiveautomation.ignition.gateway.project.ProjectLifecycle
import com.inductiveautomation.ignition.gateway.project.ProjectLifecycleFactory
import com.inductiveautomation.ignition.gateway.project.ProjectManager
import com.inductiveautomation.ignition.gateway.project.ResourceFilter
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegateFactory
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import com.mussonindustrial.ignition.embr.periscope.resources.ComponentAttributes
import com.mussonindustrial.ignition.embr.periscope.resources.ComponentResource
import java.util.concurrent.ConcurrentHashMap

class ProjectComponentRegistry(
    private val perspectiveContext: PerspectiveContext,
    projectManager: ProjectManager,
    private val delegateFactory: ComponentModelDelegateFactory,
) : ProjectLifecycleFactory<ProjectLifecycle>(projectManager) {

    private val logger = this.getLoggerEx()
    private val resourceFilter =
        ResourceFilter(ApplicationScope.ALL, listOf(ProjectComponents.VIEWS, ClientResource.type))
    private val componentsByProject = ConcurrentHashMap<String, Set<String>>()
    private val registrationsById = ConcurrentHashMap<String, Boolean>()

    override fun getResourceFilter(): ResourceFilter {
        return resourceFilter
    }

    override fun createProjectLifecycle(project: RuntimeProject): ProjectLifecycle {
        return ComponentTracker(project)
    }

    fun clear() {
        componentsByProject.clear()
        updateRegistry()
    }

    private fun updateRegistry() {
        val usedComponents = componentsByProject.values.flatten()
        (usedComponents + registrationsById.keys).distinct().forEach { updateRegistration(it) }
    }

    private fun updateRegistration(id: String) {
        registrationsById.compute(id) { _, registered ->
            val isUsed = componentsByProject.values.any { id in it }
            val isRegistered = registered != null

            when {
                isUsed && !isRegistered -> if (registerComponent(id)) true else null
                !isUsed && isRegistered -> {
                    removeComponent(id)
                    null
                }
                else -> registered
            }
        }
    }

    private fun registerComponent(id: String): Boolean {
        logger.debug("Registering React Component '$id'.")

        val descriptor = ProjectComponents.createDescriptor(id)
        return try {
            perspectiveContext.componentRegistry.registerComponent(descriptor)
            perspectiveContext.componentModelDelegateRegistry.register(
                descriptor.id(),
                delegateFactory,
            )
            true
        } catch (e: Exception) {
            logger.warn("Failed to register React Component '$id'.", e)
            perspectiveContext.componentRegistry.removeComponent(descriptor.id())
            false
        }
    }

    private fun removeComponent(id: String) {
        logger.debug("Removing React Component '$id'.")

        val type = ProjectComponents.getType(id)
        try {
            perspectiveContext.componentRegistry.removeComponent(type)
            perspectiveContext.componentModelDelegateRegistry.remove(type)
        } catch (e: Exception) {
            logger.warn("Failed to remove React Component '$id'.", e)
        }
    }

    private inner class ComponentTracker(project: RuntimeProject) : ProjectLifecycle(project) {

        private val componentsByResource = ConcurrentHashMap<ResourcePath, Set<String>>()

        override fun onStartup(resources: List<ProjectResource>) {
            readResources(resources)
        }

        override fun onShutdown(resourceIds: List<ProjectResourceId>) {
            componentsByProject.remove(project.name)
            updateRegistry()
        }

        override fun onResourcesCreated(resources: List<ProjectResource>) {
            readResources(resources)
        }

        override fun onResourcesModified(resources: List<ProjectResource>) {
            readResources(resources)
        }

        override fun onResourcesDeleted(resources: List<ProjectResourceId>) {
            resources.forEach { componentsByResource.remove(it.resourcePath) }
            updateProject()
        }

        private fun readResources(resources: List<ProjectResource>) {
            resources.forEach { componentsByResource[it.resourcePath] = findComponents(it) }
            updateProject()
        }

        private fun findComponents(resource: ProjectResource): Set<String> {
            return when (resource.resourceType) {
                ProjectComponents.VIEWS -> findUsedComponents(resource)
                else -> findDefinedComponent(resource)
            }
        }

        private fun findUsedComponents(view: ProjectResource): Set<String> {
            val viewJson =
                view.getData(ProjectComponents.VIEW_FILE)?.decodeToString() ?: return emptySet()
            return ProjectComponents.findUsedComponents(viewJson).toSet()
        }

        private fun findDefinedComponent(resource: ProjectResource): Set<String> {
            if (ClientResource.getType(resource) != ComponentResource.type) {
                return emptySet()
            }

            val attributes = ComponentAttributes.fromResource(resource)
            return if (attributes.hasValidId) setOf(attributes.id) else emptySet()
        }

        private fun updateProject() {
            val components = componentsByResource.values.flatten().toSet()
            if (componentsByProject[project.name] == components) {
                return
            }

            componentsByProject[project.name] = components
            updateRegistry()
        }
    }
}
