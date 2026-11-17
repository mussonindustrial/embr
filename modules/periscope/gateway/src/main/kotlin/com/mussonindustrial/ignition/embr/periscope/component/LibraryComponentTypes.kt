package com.mussonindustrial.ignition.embr.periscope.component

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.model.ApplicationScope
import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.common.resourcecollection.ResourceFilter
import com.inductiveautomation.ignition.common.resourcecollection.ResourceId
import com.inductiveautomation.ignition.common.resourcecollection.RuntimeResourceCollection
import com.inductiveautomation.ignition.gateway.project.ProjectManager
import com.inductiveautomation.ignition.gateway.resourcecollection.ResourceCollectionLifecycle
import com.inductiveautomation.ignition.gateway.resourcecollection.ResourceCollectionLifecycleFactory
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegateFactory
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.mussonindustrial.embr.common.logging.getLoggerEx
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull

class LibraryComponentTypes(
    private val perspective: PerspectiveContext,
    private val delegateFactory: ComponentModelDelegateFactory,
    projectManager: ProjectManager,
) : ResourceCollectionLifecycleFactory<ResourceCollectionLifecycle>(projectManager) {

    private val logger = this.getLoggerEx()
    private val viewFilter = ResourceFilter(ApplicationScope.ALL, listOf(LibraryComponents.VIEWS))
    private val componentsByProject = ConcurrentHashMap<String, Set<String>>()
    private var registeredComponents = emptySet<String>()

    override fun getResourceFilter(): ResourceFilter {
        return viewFilter
    }

    override fun createLifecycle(
        collection: RuntimeResourceCollection,
    ): ResourceCollectionLifecycle {
        return Project(collection)
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

    inner class Project(private val collection: RuntimeResourceCollection) :
        ResourceCollectionLifecycle(collection) {

        private val componentsByView = mutableMapOf<String, Set<String>>()

        override fun onStartup(resources: List<Resource>) {
            readViews(resources)
        }

        override fun onShutdown(resourceIds: List<ResourceId>) {
            componentsByProject.remove(collection.name)
            updateRegistry()
        }

        override fun onResourcesCreated(resources: List<Resource>) {
            readViews(resources)
        }

        override fun onResourcesModified(resources: List<Resource>) {
            readViews(resources)
        }

        override fun onResourcesDeleted(resources: List<ResourceId>) {
            resources
                .filter { it.resourceType == LibraryComponents.VIEWS }
                .forEach { componentsByView.remove(it.resourcePath.path.toString()) }
            updateProject()
        }

        private fun readViews(resources: List<Resource>) {
            resources
                .filter { it.resourceType == LibraryComponents.VIEWS }
                .forEach { componentsByView[it.resourcePath.path.toString()] = findComponents(it) }
            updateProject()
        }

        private fun findComponents(view: Resource): Set<String> {
            val data = view.getData(LibraryComponents.VIEW_FILE).getOrNull() ?: return emptySet()
            val viewJson = data.bytesAsString ?: return emptySet()
            return LibraryComponents.usedIn(viewJson).toSet()
        }

        private fun updateProject() {
            val usedComponents = componentsByView.values.flatten().toSet()
            if (componentsByProject[collection.name] == usedComponents) {
                return
            }

            componentsByProject[collection.name] = usedComponents
            updateRegistry()
        }
    }
}
