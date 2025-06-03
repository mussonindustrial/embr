package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.project.RuntimeProject
import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.inductiveautomation.ignition.common.project.resource.ProjectResourceId
import com.inductiveautomation.ignition.gateway.project.ProjectLifecycle
import com.inductiveautomation.ignition.gateway.project.ProjectLifecycleFactory
import com.inductiveautomation.ignition.gateway.project.ProjectManager
import com.inductiveautomation.ignition.gateway.project.ResourceFilter

class ClientResourceChangeListener(
    val sessionMonitor: PerspectiveSessionMonitor,
    projectManager: ProjectManager,
) : ProjectLifecycleFactory<ProjectLifecycle>(projectManager) {

    val filter: ResourceFilter =
        ResourceFilter.newBuilder().addResourceType(ClientResource.type).build()

    override fun createProjectLifecycle(project: RuntimeProject): Notifier {
        return Notifier(project)
    }

    override fun getResourceFilter(): ResourceFilter {
        return filter
    }

    inner class Notifier(project: RuntimeProject) : ProjectLifecycle(project) {

        fun notifyClients() {
            sessionMonitor.getSessionsForProject(project.name).forEach {
                it.pages.forEach { page ->
                    page.send("periscope-client-resource-refresh", JsonObject())
                }
            }
        }

        override fun onStartup(resourceIds: List<ProjectResource>) {}

        override fun onShutdown(resourceIds: List<ProjectResourceId>) {}

        override fun onResourcesCreated(resources: List<ProjectResource>) {
            notifyClients()
        }

        override fun onResourcesModified(resources: List<ProjectResource>) {
            notifyClients()
        }

        override fun onResourcesDeleted(resources: List<ProjectResourceId>) {
            notifyClients()
        }
    }
}
