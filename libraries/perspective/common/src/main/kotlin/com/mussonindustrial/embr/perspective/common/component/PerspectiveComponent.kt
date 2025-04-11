package com.mussonindustrial.embr.perspective.common.component

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptor

interface PerspectiveComponent {
    val id: String
    val schema: JsonSchema
    val descriptor: ComponentDescriptor
}
