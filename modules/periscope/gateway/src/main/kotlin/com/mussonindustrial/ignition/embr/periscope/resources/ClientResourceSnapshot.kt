package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.mussonindustrial.ignition.embr.periscope.component.ProjectComponentDelegateScript

class ClientResourceSnapshot(
    val hash: String,
    val resourcesByPath: Map<String, ProjectResource>,
    val delegateScriptsByComponent: Map<String, ProjectComponentDelegateScript>,
) {

    val resources: Collection<ProjectResource>
        get() = resourcesByPath.values
}
