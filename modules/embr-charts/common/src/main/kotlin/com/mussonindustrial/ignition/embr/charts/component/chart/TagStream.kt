package com.mussonindustrial.ignition.embr.charts.component.chart

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.ignition.embr.charts.Components
import com.mussonindustrial.ignition.embr.common.Embr
import com.mussonindustrial.ignition.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.ignition.embr.perspective.common.component.addPaletteEntry

class TagStream {
    companion object {
        var COMPONENT_ID: String = "embr.chart.tag-stream"
        var SCHEMA: JsonSchema = JsonSchema.parse(Components::class.java.getResourceAsStream("/tag-stream.props.json"))

        private var VARIANT_BASE = PaletteEntry(Components::class.java,
            "tag-stream.base",
            "Tag Stream Test",
            "Tag Stream test component."
        )

        var DESCRIPTOR: ComponentDescriptor = ComponentDescriptorImpl.ComponentBuilder.newBuilder()
            .setPaletteCategory("chart")
            .setId(COMPONENT_ID)
            .setModuleId(Embr.CHARTS.id)
            .setSchema(SCHEMA)
            .setName("TagStream")
            .addPaletteEntry(VARIANT_BASE)
            .setDefaultMetaName("TagStream")
            .setResources(Components.BROWSER_RESOURCES)
            .build()
    }
}