package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.common.resourcecollection.ResourceId
import com.inductiveautomation.ignition.common.resourcecollection.RuntimeResourceCollection
import com.inductiveautomation.ignition.gateway.resourcecollection.ResourceCollectionLifecycle

class ClientResourceSessionNotifier(
    val sessionMonitor: PerspectiveSessionMonitor,
    val collection: RuntimeResourceCollection,
) : ResourceCollectionLifecycle(collection) {

    companion object {
        const val PROTOCOL = "periscope-client-resource-refresh"
    }

    fun notifyClients() {
        sessionMonitor.getSessionsForProject(collection.name).forEach {
            it.pages.forEach { page -> page.send(PROTOCOL, JsonObject()) }
        }
    }

    override fun onStartup(resources: List<Resource>) {}

    override fun onShutdown(resourceIds: List<ResourceId>) {}

    override fun onResourcesCreated(resources: List<Resource>) = notifyClients()

    override fun onResourcesModified(resources: List<Resource>) = notifyClients()

    override fun onResourcesDeleted(resources: List<ResourceId>) = notifyClients()
}
