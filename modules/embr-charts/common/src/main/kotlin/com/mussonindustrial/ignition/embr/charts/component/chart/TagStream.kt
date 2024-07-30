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
        var id: String = "embr.chart.tag-stream"
        val schema: JsonSchema = JsonSchema.parse(Components::class.java.getResourceAsStream("/tag-stream.props.json"))

        private var variantBase =
            PaletteEntry(
                Components::class.java,
                "tag-stream.base",
                "Tag Stream Test",
                "Tag Stream test component.",
            )

        var descriptor: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("chart")
                .setId(id)
                .setModuleId(Embr.CHARTS.id)
                .setSchema(schema)
                .setName("TagStream")
                .addPaletteEntry(variantBase)
                .setDefaultMetaName("TagStream")
                .setResources(Components.BROWSER_RESOURCES)
                .build()
    }
}
