package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.project.RuntimeProject
import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.inductiveautomation.ignition.common.project.resource.ProjectResourceId
import com.inductiveautomation.ignition.gateway.project.ProjectLifecycle
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.perspective.gateway.session.PerspectiveSessionMonitor
import java.io.IOException

class ClientResourceChangeNotifier(
    val sessionMonitor: PerspectiveSessionMonitor,
    project: RuntimeProject,
) : ProjectLifecycle(project) {

    companion object {
        const val PROTOCOL = "periscope-client-resource-refresh"
    }

    private val logger = this.getLoggerEx()

    fun notifyClients() {
        sessionMonitor.getSessionsForProject(project.name).forEach { session ->
            session.pages.forEach { page ->
                try {
                    page.send(PROTOCOL, JsonObject())
                } catch (e: IOException) {
                    logger.debug(
                        "Skipped client resource refresh for a page without a channel: ${e.message}",
                    )
                } catch (e: Exception) {
                    logger.warn("Failed to send client resource refresh to a page", e)
                }
            }
        }
    }

    override fun onStartup(resources: List<ProjectResource>) {}

    override fun onShutdown(resourceIds: List<ProjectResourceId>) {}

    override fun onResourcesCreated(resources: List<ProjectResource>) = notifyClients()

    override fun onResourcesModified(resources: List<ProjectResource>) = notifyClients()

    override fun onResourcesDeleted(resources: List<ProjectResourceId>) = notifyClients()
}
