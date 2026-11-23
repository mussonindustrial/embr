package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.designer.model.DesignerContext

fun DesignerContext.findComponentResources(): List<Resource> {
    val resources = project?.getResourcesOfType(ClientResource.type).orEmpty()
    return resources.filter { ClientResource.getType(it) == ComponentResource.type }
}
