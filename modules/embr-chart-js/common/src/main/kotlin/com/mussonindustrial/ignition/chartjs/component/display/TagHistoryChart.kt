package com.mussonindustrial.ignition.chartjs.component.display

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.mussonindustrial.ignition.chartjs.Components
import com.mussonindustrial.ignition.chartjs.Components.COMPONENT_CATEGORY
import com.mussonindustrial.ignition.chartjs.Meta.MODULE_ID

class TagHistoryChart {
    companion object {
        var COMPONENT_ID: String = "mussonindustrial.chart.chart-js.taghistory"
        var SCHEMA: JsonSchema = JsonSchema.parse(Components::class.java.getResourceAsStream("/chart-js.props.json"))
        var DESCRIPTOR: ComponentDescriptor = ComponentDescriptorImpl.ComponentBuilder.newBuilder()
            .setPaletteCategory(COMPONENT_CATEGORY)
            .setId(COMPONENT_ID)
            .setModuleId(MODULE_ID)
            .setSchema(SCHEMA)
            .setName("Tag History Chart")
//            .setIcon(ImageIcon(Components::class.java.getResource("/icons/simplecomponent.png")))
            .addPaletteEntry("", "Tag History Chart", "Tag History Chart", null, null)
            .setDefaultMetaName("Tag History Chart")
            .setResources(Components.BROWSER_RESOURCES)
            .build()
    }
}