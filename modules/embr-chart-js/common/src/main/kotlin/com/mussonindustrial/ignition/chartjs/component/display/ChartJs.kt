package com.mussonindustrial.ignition.chartjs.component.display

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.mussonindustrial.ignition.chartjs.Components
import com.mussonindustrial.ignition.chartjs.Components.COMPONENT_CATEGORY
import com.mussonindustrial.ignition.chartjs.Meta.MODULE_ID

class ChartJs {
    companion object {
        var COMPONENT_ID: String = "mussonindustrial.chart.chart-js"
        var SCHEMA: JsonSchema = JsonSchema.parse(Components::class.java.getResourceAsStream("/chart-js.props2.json"))
        var DESCRIPTOR: ComponentDescriptor = ComponentDescriptorImpl.ComponentBuilder.newBuilder()
            .setPaletteCategory(COMPONENT_CATEGORY)
            .setId(COMPONENT_ID)
            .setModuleId(MODULE_ID)
            .setSchema(SCHEMA)
            .setName("Chart.js")
//            .setIcon(ImageIcon(Components::class.java.getResource("/icons/simplecomponent.png")))
            .addPaletteEntry("", "Chart.js", "Chart.js Component", null, null)
            .setDefaultMetaName("chart_js")
            .setResources(Components.BROWSER_RESOURCES)
            .build()
    }
}