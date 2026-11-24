package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.model.ApplicationScope
import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.common.resourcecollection.ResourceFilter
import com.inductiveautomation.ignition.common.resourcecollection.ResourceId
import com.inductiveautomation.ignition.common.resourcecollection.ResourcePath
import com.inductiveautomation.ignition.common.resourcecollection.ResourceType
import com.inductiveautomation.ignition.common.resourcecollection.RuntimeResourceCollection
import com.inductiveautomation.ignition.gateway.project.ProjectManager
import com.inductiveautomation.ignition.gateway.resourcecollection.ResourceCollectionLifecycle
import com.inductiveautomation.ignition.gateway.resourcecollection.ResourceCollectionLifecycleFactory
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.ignition.embr.periscope.component.ProjectComponentDelegateScript
import com.mussonindustrial.ignition.embr.periscope.utils.getClientResourceHash
import java.util.concurrent.ConcurrentHashMap

class ClientResourceSnapshotManager(
    projectManager: ProjectManager,
    private val getRuntimeHash: () -> String,
) : ResourceCollectionLifecycleFactory<ResourceCollectionLifecycle>(projectManager) {

    companion object {
        private val SCRIPT_LIBRARY = ResourceType("ignition", "script-python")
    }

    private val logger = this.getLoggerEx()
    private val resourceFilter =
        ResourceFilter(ApplicationScope.ALL, listOf(ClientResource.type, SCRIPT_LIBRARY))
    private val snapshotsByProject = ConcurrentHashMap<String, ClientResourceSnapshot>()
    private val listenersByProject = ConcurrentHashMap<String, MutableSet<() -> Unit>>()

    override fun getResourceFilter(): ResourceFilter {
        return resourceFilter
    }

    override fun createLifecycle(
        collection: RuntimeResourceCollection,
    ): ResourceCollectionLifecycle {
        return SnapshotTracker(collection)
    }

    fun find(projectName: String): ClientResourceSnapshot? {
        return snapshotsByProject[projectName]
    }

    fun addListener(projectName: String, listener: () -> Unit) {
        listenersByProject
            .computeIfAbsent(projectName) { ConcurrentHashMap.newKeySet() }
            .add(listener)
    }

    fun removeListener(projectName: String, listener: () -> Unit) {
        listenersByProject[projectName]?.remove(listener)
    }

    private inner class SnapshotTracker(private val collection: RuntimeResourceCollection) :
        ResourceCollectionLifecycle(collection) {

        private val resourcesByPath = ConcurrentHashMap<ResourcePath, Resource>()

        override fun onStartup(resources: List<Resource>) {
            storeResources(resources)
            updateSnapshot()
        }

        override fun onShutdown(resourceIds: List<ResourceId>) {
            snapshotsByProject.remove(collection.name)
        }

        override fun onResourcesCreated(resources: List<Resource>) {
            storeResources(resources)
            onChanged()
        }

        override fun onResourcesModified(resources: List<Resource>) {
            storeResources(resources)
            onChanged()
        }

        override fun onResourcesDeleted(resources: List<ResourceId>) {
            resources.forEach { resourcesByPath.remove(it.resourcePath) }
            onChanged()
        }

        private fun storeResources(resources: List<Resource>) {
            resources
                .filter { it.resourceType == ClientResource.type }
                .forEach { resourcesByPath[it.resourcePath] = it }
        }

        private fun onChanged() {
            updateSnapshot()
            listenersByProject[collection.name]?.forEach { it() }
        }

        private fun updateSnapshot() {
            val resources = resourcesByPath.values.sortedBy { it.resourcePath.pathString }
            val previous = snapshotsByProject[collection.name]

            snapshotsByProject[collection.name] =
                ClientResourceSnapshot(
                    resources.getClientResourceHash(getRuntimeHash()),
                    resources.associateBy { it.resourcePath.pathString },
                    compileDelegates(resources, previous?.delegateScriptsByComponent.orEmpty()),
                )
        }

        private fun compileDelegates(
            resources: List<Resource>,
            previous: Map<String, ProjectComponentDelegateScript>,
        ): Map<String, ProjectComponentDelegateScript> {
            val firstComponentsById =
                resources
                    .filter { ClientResource.getType(it) == ComponentResource.type }
                    .groupBy { ComponentAttributes.fromResource(it).id }
                    .mapValues { (_, components) -> components.first() }

            return firstComponentsById
                .mapNotNull { (id, component) ->
                    compileDelegate(component, previous[id])?.let { id to it }
                }
                .toMap()
        }

        private fun compileDelegate(
            component: Resource,
            previous: ProjectComponentDelegateScript?,
        ): ProjectComponentDelegateScript? {
            val path = component.resourcePath.pathString
            val source = ComponentResource.getDelegate(component)
            if (!ComponentResource.isDelegateEnabled(component) || source.isBlank()) {
                return null
            }
            if (previous != null && previous.path == path && previous.source == source) {
                return previous
            }

            return try {
                ProjectComponentDelegateScript(path, source)
            } catch (e: Exception) {
                logger.warn("Failed to compile the delegate of '$path'.", e)
                null
            }
        }
    }
}
