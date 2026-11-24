package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.model.ApplicationScope
import com.inductiveautomation.ignition.common.project.RuntimeProject
import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.inductiveautomation.ignition.common.project.resource.ProjectResourceId
import com.inductiveautomation.ignition.common.project.resource.ResourcePath
import com.inductiveautomation.ignition.common.project.resource.ResourceType
import com.inductiveautomation.ignition.gateway.project.ProjectLifecycle
import com.inductiveautomation.ignition.gateway.project.ProjectLifecycleFactory
import com.inductiveautomation.ignition.gateway.project.ProjectManager
import com.inductiveautomation.ignition.gateway.project.ResourceFilter
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.ignition.embr.periscope.component.ProjectComponentDelegateScript
import com.mussonindustrial.ignition.embr.periscope.utils.getClientResourceHash
import java.util.concurrent.ConcurrentHashMap

class ClientResourceSnapshotManager(
    projectManager: ProjectManager,
    private val getRuntimeHash: () -> String,
) : ProjectLifecycleFactory<ProjectLifecycle>(projectManager) {

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

    override fun createProjectLifecycle(project: RuntimeProject): ProjectLifecycle {
        return SnapshotTracker(project)
    }

    fun find(projectName: String): ClientResourceSnapshot? {
        return snapshotsByProject[projectName]
    }

    /** Listeners run while the project is locked for a change, so they must not wait on it. */
    fun addListener(projectName: String, listener: () -> Unit) {
        listenersByProject
            .computeIfAbsent(projectName) { ConcurrentHashMap.newKeySet() }
            .add(listener)
    }

    fun removeListener(projectName: String, listener: () -> Unit) {
        listenersByProject[projectName]?.remove(listener)
    }

    private inner class SnapshotTracker(private val project: RuntimeProject) :
        ProjectLifecycle(project) {

        private val resourcesByPath = ConcurrentHashMap<ResourcePath, ProjectResource>()

        override fun onStartup(resources: List<ProjectResource>) {
            storeResources(resources)
            updateSnapshot()
        }

        override fun onShutdown(resourceIds: List<ProjectResourceId>) {
            snapshotsByProject.remove(project.name)
        }

        override fun onResourcesCreated(resources: List<ProjectResource>) {
            storeResources(resources)
            onChanged()
        }

        override fun onResourcesModified(resources: List<ProjectResource>) {
            storeResources(resources)
            onChanged()
        }

        override fun onResourcesDeleted(resources: List<ProjectResourceId>) {
            resources.forEach { resourcesByPath.remove(it.resourcePath) }
            onChanged()
        }

        private fun storeResources(resources: List<ProjectResource>) {
            resources
                .filter { it.resourceType == ClientResource.type }
                .forEach { resourcesByPath[it.resourcePath] = it }
        }

        private fun onChanged() {
            updateSnapshot()
            listenersByProject[project.name]?.forEach { it() }
        }

        private fun updateSnapshot() {
            val resources = resourcesByPath.values.sortedBy { it.resourcePath.pathString }
            val previous = snapshotsByProject[project.name]

            snapshotsByProject[project.name] =
                ClientResourceSnapshot(
                    resources.getClientResourceHash(getRuntimeHash()),
                    resources.associateBy { it.resourcePath.pathString },
                    compileDelegates(resources, previous?.delegateScriptsByComponent.orEmpty()),
                )
        }

        /** The first resource by path owns a Component ID, as it does in the browser. */
        private fun compileDelegates(
            resources: List<ProjectResource>,
            previous: Map<String, ProjectComponentDelegateScript>,
        ): Map<String, ProjectComponentDelegateScript> {
            val componentsById =
                resources
                    .filter { ClientResource.getType(it) == ComponentResource.type }
                    .groupBy { ComponentAttributes.fromResource(it).id }
                    .mapValues { (_, components) -> components.first() }

            return componentsById
                .mapNotNull { (id, component) ->
                    compileDelegate(component, previous[id])?.let { id to it }
                }
                .toMap()
        }

        private fun compileDelegate(
            component: ProjectResource,
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
