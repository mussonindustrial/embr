package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.model.ApplicationScope
import com.inductiveautomation.ignition.common.resourcecollection.ResourceFilter
import com.inductiveautomation.ignition.common.resourcecollection.RuntimeResourceCollection
import com.inductiveautomation.ignition.gateway.project.ProjectManager
import com.inductiveautomation.ignition.gateway.resourcecollection.ResourceCollectionLifecycle
import com.inductiveautomation.ignition.gateway.resourcecollection.ResourceCollectionLifecycleFactory
import com.mussonindustrial.embr.perspective.gateway.session.PerspectiveSessionMonitor

class ClientResourceChangeListener(
    val sessionMonitor: PerspectiveSessionMonitor,
    projectManager: ProjectManager,
) : ResourceCollectionLifecycleFactory<ResourceCollectionLifecycle>(projectManager) {

    val filter = ResourceFilter(ApplicationScope.ALL, listOf(ClientResource.type))

    override fun getResourceFilter(): ResourceFilter {
        return filter
    }

    override fun createLifecycle(
        collection: RuntimeResourceCollection
    ): ResourceCollectionLifecycle {
        return ClientResourceChangeNotifier(sessionMonitor, collection)
    }
}
