package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.project.RuntimeProject
import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.inductiveautomation.ignition.common.project.resource.ProjectResourceId
import com.inductiveautomation.ignition.gateway.project.ProjectLifecycle

class ClientResourceSessionNotifier(
    val sessionMonitor: PerspectiveSessionMonitor,
    project: RuntimeProject,
) : ProjectLifecycle(project) {

    companion object {
        const val PROTOCOL = "periscope-client-resource-refresh"
    }

    fun notifyClients() {
        sessionMonitor.getSessionsForProject(project.name).forEach {
            it.pages.forEach { page -> page.send(PROTOCOL, JsonObject()) }
        }
    }

    override fun onStartup(resources: List<ProjectResource>) {}

    override fun onShutdown(resourceIds: List<ProjectResourceId>) {}

    override fun onResourcesCreated(resources: List<ProjectResource>) = notifyClients()

    override fun onResourcesModified(resources: List<ProjectResource>) = notifyClients()

    override fun onResourcesDeleted(resources: List<ProjectResourceId>) = notifyClients()
}
