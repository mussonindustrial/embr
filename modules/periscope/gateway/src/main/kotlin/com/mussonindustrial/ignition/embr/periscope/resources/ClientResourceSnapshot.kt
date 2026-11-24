package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.mussonindustrial.ignition.embr.periscope.component.ProjectComponentDelegateScript

class ClientResourceSnapshot(
    val hash: String,
    val resourcesByPath: Map<String, Resource>,
    val delegateScriptsByComponent: Map<String, ProjectComponentDelegateScript>,
) {

    val resources: Collection<Resource>
        get() = resourcesByPath.values
}
