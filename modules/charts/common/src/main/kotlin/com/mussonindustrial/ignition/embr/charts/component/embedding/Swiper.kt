package com.mussonindustrial.ignition.embr.charts.component.embedding

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.ignition.embr.charts.Components
import com.mussonindustrial.ignition.embr.charts.Meta.MODULE_ID
import com.mussonindustrial.ignition.embr.charts.component.PaletteEntry
import com.mussonindustrial.ignition.embr.charts.component.addPaletteEntry

class Swiper {
    companion object {
        var COMPONENT_ID: String = "embr.chart.swiper"
        var SCHEMA: JsonSchema =
            JsonSchema.parse(Components::class.java.getResourceAsStream("/swiper.props.json"))

        private var VARIANT_BASE =
            PaletteEntry("swiper.base", "Swiper", "The Most Modern Mobile Touch Slider.")

        var DESCRIPTOR: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("embedding")
                .setId(COMPONENT_ID)
                .setModuleId(MODULE_ID)
                .setSchema(SCHEMA)
                .setName("Swiper")
                .addPaletteEntry(VARIANT_BASE)
                .setDefaultMetaName("Swiper")
                .setResources(Components.BROWSER_RESOURCES)
                .build()
    }
}
