package com.mussonindustrial.ignition.chartjs.component.display

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.mussonindustrial.ignition.chartjs.Components
import com.mussonindustrial.ignition.chartjs.Meta.MODULE_ID
import javax.imageio.ImageIO
import javax.swing.ImageIcon

class ChartJs {
    companion object {
        var COMPONENT_ID: String = "mussonindustrial.chart.chart-js"
        var SCHEMA: JsonSchema = JsonSchema.parse(Components::class.java.getResourceAsStream("/chart-js.props.json"))
        var DESCRIPTOR: ComponentDescriptor = ComponentDescriptorImpl.ComponentBuilder.newBuilder()
            .setPaletteCategory("chart")
            .setId(COMPONENT_ID)
            .setModuleId(MODULE_ID)
            .setSchema(SCHEMA)
            .setName("Chart.js Chart")
            .setIcon(ImageIcon(Components::class.java.getResource("/icons/chartjs_16.png")))
            .addPaletteEntry("", "Chart.js", "A simple yet flexible JavaScript charting library for the modern web.", ImageIO.read(Components::class.java.getResource("/icons/chartjs_thumbnail.png")), null)
            .setDefaultMetaName("Chartjs")
            .setResources(Components.BROWSER_RESOURCES)
            .build()
    }
}