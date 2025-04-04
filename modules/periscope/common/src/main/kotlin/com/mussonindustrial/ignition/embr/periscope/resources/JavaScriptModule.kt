package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.project.resource.ResourceType
import com.mussonindustrial.ignition.embr.periscope.Meta

class JavaScriptModule {

    companion object {
        const val TYPE_ID: String = "javascript-module"
        val RESOURCE_TYPE: ResourceType = ResourceType(Meta.MODULE_ID, TYPE_ID)
        const val DATA_KEY: String = "script.js"
    }
}
