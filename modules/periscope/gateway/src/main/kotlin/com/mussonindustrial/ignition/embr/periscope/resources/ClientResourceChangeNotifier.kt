package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.common.resourcecollection.ResourceId
import com.inductiveautomation.ignition.common.resourcecollection.RuntimeResourceCollection
import com.inductiveautomation.ignition.gateway.resourcecollection.ResourceCollectionLifecycle
import com.inductiveautomation.perspective.gateway.model.MessageChannelException
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.perspective.gateway.session.PerspectiveSessionMonitor

class ClientResourceChangeNotifier(
    val sessionMonitor: PerspectiveSessionMonitor,
    val collection: RuntimeResourceCollection,
) : ResourceCollectionLifecycle(collection) {

    companion object {
        const val PROTOCOL = "periscope-client-resource-refresh"
    }

    private val logger = this.getLoggerEx()

    fun notifyClients() {
        sessionMonitor.getSessionsForProject(collection.name).forEach { session ->
            session.pages.forEach { page ->
                try {
                    page.send(PROTOCOL, JsonObject())
                } catch (e: MessageChannelException) {
                    logger.debug(
                        "Skipped client resource refresh for a page without a channel: ${e.message}",
                    )
                } catch (e: Exception) {
                    logger.warn("Failed to send a client resource refresh to a page.", e)
                }
            }
        }
    }

    override fun onStartup(resources: List<Resource>) {}

    override fun onShutdown(resourceIds: List<ResourceId>) {}

    override fun onResourcesCreated(resources: List<Resource>) = notifyClients()

    override fun onResourcesModified(resources: List<Resource>) = notifyClients()

    override fun onResourcesDeleted(resources: List<ResourceId>) = notifyClients()
}
