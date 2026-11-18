package com.mussonindustrial.ignition.embr.periscope.component

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.model.ApplicationScope
import com.inductiveautomation.ignition.common.project.RuntimeProject
import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.inductiveautomation.ignition.common.project.resource.ProjectResourceId
import com.inductiveautomation.ignition.gateway.project.ProjectLifecycle
import com.inductiveautomation.ignition.gateway.project.ProjectLifecycleFactory
import com.inductiveautomation.ignition.gateway.project.ProjectManager
import com.inductiveautomation.ignition.gateway.project.ResourceFilter
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegateFactory
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.mussonindustrial.embr.common.logging.getLoggerEx
import java.util.concurrent.ConcurrentHashMap

class LibraryComponentTypes(
    private val perspective: PerspectiveContext,
    private val delegateFactory: ComponentModelDelegateFactory,
    projectManager: ProjectManager,
) : ProjectLifecycleFactory<ProjectLifecycle>(projectManager) {

    private val logger = this.getLoggerEx()
    private val viewFilter = ResourceFilter(ApplicationScope.ALL, listOf(LibraryComponents.VIEWS))
    private val componentsByProject = ConcurrentHashMap<String, Set<String>>()
    private var registeredComponents = emptySet<String>()

    override fun getResourceFilter(): ResourceFilter {
        return viewFilter
    }

    override fun createProjectLifecycle(project: RuntimeProject): ProjectLifecycle {
        return Project(project)
    }

    fun clear() {
        componentsByProject.clear()
        updateRegistry()
    }

    @Synchronized
    private fun updateRegistry() {
        val usedComponents = componentsByProject.values.flatten().toSet()
        if (usedComponents == registeredComponents) {
            return
        }

        (registeredComponents - usedComponents).forEach { removeComponent(it) }
        (usedComponents - registeredComponents).forEach { addComponent(it) }

        registeredComponents = usedComponents
    }

    private fun addComponent(id: String) {
        logger.debug("Registering library component $id")

        try {
            val descriptor =
                LibraryComponents.descriptor(
                    id = id,
                    name = id,
                    schema = LibraryComponents.LOOSE_SCHEMA,
                    defaults = JsonObject(),
                    events = emptyList(),
                    children = null,
                )

            perspective.componentRegistry.registerComponent(descriptor)
            perspective.componentModelDelegateRegistry.register(
                LibraryComponents.typeOf(id),
                delegateFactory,
            )
        } catch (e: Throwable) {
            logger.warn("Failed to register library component $id", e)
        }
    }

    private fun removeComponent(id: String) {
        logger.debug("Removing library component $id")

        val type = LibraryComponents.typeOf(id)
        perspective.componentRegistry.removeComponent(type)
        perspective.componentModelDelegateRegistry.remove(type)
    }

    inner class Project(project: RuntimeProject) : ProjectLifecycle(project) {

        private val componentsByView = mutableMapOf<String, Set<String>>()

        override fun onStartup(resources: List<ProjectResource>) {
            readViews(resources)
        }

        override fun onShutdown(resourceIds: List<ProjectResourceId>) {
            componentsByProject.remove(project.name)
            updateRegistry()
        }

        override fun onResourcesCreated(resources: List<ProjectResource>) {
            readViews(resources)
        }

        override fun onResourcesModified(resources: List<ProjectResource>) {
            readViews(resources)
        }

        override fun onResourcesDeleted(resources: List<ProjectResourceId>) {
            resources
                .filter { it.resourceType == LibraryComponents.VIEWS }
                .forEach { componentsByView.remove(it.resourcePath.path.toString()) }
            updateProject()
        }

        private fun readViews(resources: List<ProjectResource>) {
            resources
                .filter { it.resourceType == LibraryComponents.VIEWS }
                .forEach { componentsByView[it.resourcePath.path.toString()] = findComponents(it) }
            updateProject()
        }

        private fun findComponents(view: ProjectResource): Set<String> {
            val viewJson =
                view.getData(LibraryComponents.VIEW_FILE)?.decodeToString() ?: return emptySet()
            return LibraryComponents.usedIn(viewJson).toSet()
        }

        private fun updateProject() {
            val usedComponents = componentsByView.values.flatten().toSet()
            if (componentsByProject[project.name] == usedComponents) {
                return
            }

            componentsByProject[project.name] = usedComponents
            updateRegistry()
        }
    }
}
