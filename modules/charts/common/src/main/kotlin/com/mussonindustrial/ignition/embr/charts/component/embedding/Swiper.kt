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
        private var VARIANT_AUTO_HORIZONTAL =
            PaletteEntry(
                "swiper.auto-horizontal",
                "Auto Horizontal",
                "Auto-sized horizontal slides."
            )
        private var VARIANT_AUTO_VERTICAL =
            PaletteEntry("swiper.auto-vertical", "Auto Vertical", "Auto-sized vertical slides.")
        private var VARIANT_FULL_HORIZONTAL =
            PaletteEntry(
                "swiper.full-horizontal",
                "Full Horizontal",
                "Full-sized horizontal slides."
            )
        private var VARIANT_FULL_VERTICAL =
            PaletteEntry("swiper.full-vertical", "Full Vertical", "Full-sized vertical slides.")

        var DESCRIPTOR: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("embedding")
                .setId(COMPONENT_ID)
                .setModuleId(MODULE_ID)
                .setSchema(SCHEMA)
                .setName("Swiper")
                .addPaletteEntry(VARIANT_BASE)
                .addPaletteEntry(VARIANT_AUTO_HORIZONTAL)
                .addPaletteEntry(VARIANT_AUTO_VERTICAL)
                .addPaletteEntry(VARIANT_FULL_HORIZONTAL)
                .addPaletteEntry(VARIANT_FULL_VERTICAL)
                .setDefaultMetaName("Swiper")
                .setResources(Components.BROWSER_RESOURCES)
                .build()
    }
}
