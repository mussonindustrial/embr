package com.mussonindustrial.ignition.embr.periscope.component

import com.inductiveautomation.ignition.common.model.ApplicationScope
import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.common.resourcecollection.ResourceFilter
import com.inductiveautomation.ignition.common.resourcecollection.ResourceId
import com.inductiveautomation.ignition.common.resourcecollection.ResourceType
import com.inductiveautomation.ignition.common.resourcecollection.RuntimeResourceCollection
import com.inductiveautomation.ignition.gateway.project.ProjectManager
import com.inductiveautomation.ignition.gateway.resourcecollection.ResourceCollectionLifecycle
import com.inductiveautomation.ignition.gateway.resourcecollection.ResourceCollectionLifecycleFactory
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import com.mussonindustrial.ignition.embr.periscope.resources.ComponentAttributes
import com.mussonindustrial.ignition.embr.periscope.resources.ComponentResource
import com.mussonindustrial.ignition.embr.periscope.resources.pathString
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull

class ProjectComponentDelegateScriptManager(projectManager: ProjectManager) :
    ResourceCollectionLifecycleFactory<ResourceCollectionLifecycle>(projectManager) {

    companion object {
        private val SCRIPT_LIBRARY = ResourceType("ignition", "script-python")
    }

    private val logger = this.getLoggerEx()
    private val resourceFilter =
        ResourceFilter(ApplicationScope.ALL, listOf(ClientResource.type, SCRIPT_LIBRARY))
    private val cachesByProject = ConcurrentHashMap<String, ScriptCache>()
    private val listenersByProject = ConcurrentHashMap<String, MutableSet<() -> Unit>>()

    override fun getResourceFilter(): ResourceFilter {
        return resourceFilter
    }

    override fun createLifecycle(
        collection: RuntimeResourceCollection,
    ): ResourceCollectionLifecycle {
        return ScriptCache(collection)
    }

    fun find(projectName: String, componentId: String): ProjectComponentDelegateScript? {
        return cachesByProject[projectName]?.find(componentId)
    }

    fun addListener(projectName: String, listener: () -> Unit) {
        listenersByProject
            .computeIfAbsent(projectName) { ConcurrentHashMap.newKeySet() }
            .add(listener)
    }

    fun removeListener(projectName: String, listener: () -> Unit) {
        listenersByProject[projectName]?.remove(listener)
    }

    private inner class ScriptCache(private val collection: RuntimeResourceCollection) :
        ResourceCollectionLifecycle(collection) {

        private val scriptsByComponent =
            ConcurrentHashMap<String, Optional<ProjectComponentDelegateScript>>()

        override fun onStartup(resources: List<Resource>) {
            cachesByProject[collection.name] = this
        }

        override fun onShutdown(resourceIds: List<ResourceId>) {
            cachesByProject.remove(collection.name)
        }

        override fun onResourcesCreated(resources: List<Resource>) {
            onChanged()
        }

        override fun onResourcesModified(resources: List<Resource>) {
            onChanged()
        }

        override fun onResourcesDeleted(resources: List<ResourceId>) {
            onChanged()
        }

        fun find(componentId: String): ProjectComponentDelegateScript? {
            return scriptsByComponent
                .computeIfAbsent(componentId) { Optional.ofNullable(compile(it)) }
                .getOrNull()
        }

        private fun onChanged() {
            scriptsByComponent.clear()
            listenersByProject[collection.name]?.forEach { it() }
        }

        private fun compile(componentId: String): ProjectComponentDelegateScript? {
            val resource = findComponent(componentId) ?: return null
            val path = resource.resourcePath.pathString
            val source = ComponentResource.getDelegate(resource)
            if (source.isBlank()) {
                return null
            }

            return try {
                ProjectComponentDelegateScript(path, source)
            } catch (e: Exception) {
                logger.warn("Failed to compile the delegate of '$path'.", e)
                null
            }
        }

        private fun findComponent(componentId: String): Resource? {
            return collection
                .getResourcesOfType(ClientResource.type)
                .filter { ClientResource.getType(it) == ComponentResource.type }
                .sortedBy { it.resourcePath.pathString }
                .find { ComponentAttributes.fromResource(it).id == componentId }
        }
    }
}
