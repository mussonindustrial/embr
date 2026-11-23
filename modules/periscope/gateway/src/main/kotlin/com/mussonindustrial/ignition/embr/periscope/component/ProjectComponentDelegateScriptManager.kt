package com.mussonindustrial.ignition.embr.periscope.component

import com.inductiveautomation.ignition.common.model.ApplicationScope
import com.inductiveautomation.ignition.common.project.RuntimeProject
import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.inductiveautomation.ignition.common.project.resource.ProjectResourceId
import com.inductiveautomation.ignition.common.project.resource.ResourceType
import com.inductiveautomation.ignition.gateway.project.ProjectLifecycle
import com.inductiveautomation.ignition.gateway.project.ProjectLifecycleFactory
import com.inductiveautomation.ignition.gateway.project.ProjectManager
import com.inductiveautomation.ignition.gateway.project.ResourceFilter
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import com.mussonindustrial.ignition.embr.periscope.resources.ComponentAttributes
import com.mussonindustrial.ignition.embr.periscope.resources.ComponentResource
import com.mussonindustrial.ignition.embr.periscope.resources.pathString
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull

class ProjectComponentDelegateScriptManager(projectManager: ProjectManager) :
    ProjectLifecycleFactory<ProjectLifecycle>(projectManager) {

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

    override fun createProjectLifecycle(project: RuntimeProject): ProjectLifecycle {
        return ScriptCache(project)
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

    private inner class ScriptCache(private val project: RuntimeProject) :
        ProjectLifecycle(project) {

        private val scriptsByComponent =
            ConcurrentHashMap<String, Optional<ProjectComponentDelegateScript>>()

        override fun onStartup(resources: List<ProjectResource>) {
            cachesByProject[project.name] = this
        }

        override fun onShutdown(resourceIds: List<ProjectResourceId>) {
            cachesByProject.remove(project.name)
        }

        override fun onResourcesCreated(resources: List<ProjectResource>) {
            onChanged()
        }

        override fun onResourcesModified(resources: List<ProjectResource>) {
            onChanged()
        }

        override fun onResourcesDeleted(resources: List<ProjectResourceId>) {
            onChanged()
        }

        fun find(componentId: String): ProjectComponentDelegateScript? {
            return scriptsByComponent
                .computeIfAbsent(componentId) { Optional.ofNullable(compile(it)) }
                .getOrNull()
        }

        private fun onChanged() {
            scriptsByComponent.clear()
            listenersByProject[project.name]?.forEach { it() }
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

        private fun findComponent(componentId: String): ProjectResource? {
            return project
                .getResourcesOfType(ClientResource.type)
                .filter { ClientResource.getType(it) == ComponentResource.type }
                .sortedBy { it.resourcePath.pathString }
                .find { ComponentAttributes.fromResource(it).id == componentId }
        }
    }
}
