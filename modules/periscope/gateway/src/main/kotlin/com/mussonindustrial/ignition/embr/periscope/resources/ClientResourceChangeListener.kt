package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.model.ApplicationScope
import com.inductiveautomation.ignition.common.project.RuntimeProject
import com.inductiveautomation.ignition.gateway.project.ProjectLifecycle
import com.inductiveautomation.ignition.gateway.project.ProjectLifecycleFactory
import com.inductiveautomation.ignition.gateway.project.ProjectManager
import com.inductiveautomation.ignition.gateway.project.ResourceFilter
import com.mussonindustrial.embr.perspective.gateway.session.PerspectiveSessionMonitor

class ClientResourceChangeListener(
    val sessionMonitor: PerspectiveSessionMonitor,
    projectManager: ProjectManager,
) : ProjectLifecycleFactory<ProjectLifecycle>(projectManager) {

    val filter = ResourceFilter(ApplicationScope.ALL, listOf(ClientResource.type))

    override fun getResourceFilter(): ResourceFilter {
        return filter
    }

    override fun createProjectLifecycle(project: RuntimeProject): ProjectLifecycle {
        return ClientResourceChangeNotifier(sessionMonitor, project)
    }
}
