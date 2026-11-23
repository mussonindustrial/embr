package com.mussonindustrial.ignition.embr.periscope.component

import com.inductiveautomation.ignition.common.model.ApplicationScope
import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.common.resourcecollection.ResourceFilter
import com.inductiveautomation.ignition.common.resourcecollection.ResourceId
import com.inductiveautomation.ignition.common.resourcecollection.ResourcePath
import com.inductiveautomation.ignition.common.resourcecollection.RuntimeResourceCollection
import com.inductiveautomation.ignition.gateway.project.ProjectManager
import com.inductiveautomation.ignition.gateway.resourcecollection.ResourceCollectionLifecycle
import com.inductiveautomation.ignition.gateway.resourcecollection.ResourceCollectionLifecycleFactory
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegateFactory
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import com.mussonindustrial.ignition.embr.periscope.resources.ComponentAttributes
import com.mussonindustrial.ignition.embr.periscope.resources.ComponentResource
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull

class ProjectComponentRegistry(
    private val perspectiveContext: PerspectiveContext,
    projectManager: ProjectManager,
    private val delegateFactory: ComponentModelDelegateFactory,
) : ResourceCollectionLifecycleFactory<ResourceCollectionLifecycle>(projectManager) {

    private val logger = this.getLoggerEx()
    private val resourceFilter =
        ResourceFilter(ApplicationScope.ALL, listOf(ProjectComponents.VIEWS, ClientResource.type))
    private val componentsByProject = ConcurrentHashMap<String, Set<String>>()
    private var registeredComponents = emptySet<String>()

    override fun getResourceFilter(): ResourceFilter {
        return resourceFilter
    }

    override fun createLifecycle(
        collection: RuntimeResourceCollection,
    ): ResourceCollectionLifecycle {
        return ComponentTracker(collection)
    }

    fun clear() {
        componentsByProject.clear()
        updateRegistry()
    }

    @Synchronized
    private fun updateRegistry() {
        val components = componentsByProject.values.flatten().toSet()
        if (components == registeredComponents) {
            return
        }

        (registeredComponents - components).forEach { removeComponent(it) }

        val added = mutableSetOf<String>()
        (components - registeredComponents).forEach {
            if (registerComponent(it)) {
                added += it
            }
        }

        registeredComponents = (registeredComponents intersect components) + added
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

    private inner class ComponentTracker(private val collection: RuntimeResourceCollection) :
        ResourceCollectionLifecycle(collection) {

        private val componentsByResource = ConcurrentHashMap<ResourcePath, Set<String>>()

        override fun onStartup(resources: List<Resource>) {
            readResources(resources)
        }

        override fun onShutdown(resourceIds: List<ResourceId>) {
            componentsByProject.remove(collection.name)
            updateRegistry()
        }

        override fun onResourcesCreated(resources: List<Resource>) {
            readResources(resources)
        }

        override fun onResourcesModified(resources: List<Resource>) {
            readResources(resources)
        }

        override fun onResourcesDeleted(resources: List<ResourceId>) {
            resources.forEach { componentsByResource.remove(it.resourcePath) }
            updateProject()
        }

        private fun readResources(resources: List<Resource>) {
            resources.forEach { componentsByResource[it.resourcePath] = findComponents(it) }
            updateProject()
        }

        private fun findComponents(resource: Resource): Set<String> {
            return when (resource.resourceType) {
                ProjectComponents.VIEWS -> findUsedComponents(resource)
                else -> findDefinedComponent(resource)
            }
        }

        private fun findUsedComponents(view: Resource): Set<String> {
            val data = view.getData(ProjectComponents.VIEW_FILE).getOrNull() ?: return emptySet()
            val viewJson = data.bytesAsString ?: return emptySet()
            return ProjectComponents.findUsedComponents(viewJson).toSet()
        }

        private fun findDefinedComponent(resource: Resource): Set<String> {
            if (ClientResource.getType(resource) != ComponentResource.type) {
                return emptySet()
            }

            val attributes = ComponentAttributes.fromResource(resource)
            return if (attributes.hasValidId) setOf(attributes.id) else emptySet()
        }

        private fun updateProject() {
            val components = componentsByResource.values.flatten().toSet()
            if (componentsByProject[collection.name] == components) {
                return
            }

            componentsByProject[collection.name] = components
            updateRegistry()
        }
    }
}
