package com.mussonindustrial.ignition.embr.periscope.component.embedding

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.embr.perspective.common.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.periscope.Components
import com.mussonindustrial.ignition.embr.periscope.Meta.MODULE_ID

class Swiper {
    companion object {
        var COMPONENT_ID: String = "embr.periscope.embedding.swiper"
        var SCHEMA: JsonSchema =
            JsonSchema.parse(
                Components::class
                    .java
                    .getResourceAsStream(
                        "/schemas/components/embr.periscope.embedding.swiper/props.json"
                    )
            )

        private var VARIANT_BASE =
            PaletteEntry(
                this::class.java,
                COMPONENT_ID,
                "base",
                "Swiper",
                "The Most Modern Mobile Touch Slider."
            )
        private var VARIANT_AUTO_HORIZONTAL =
            PaletteEntry(
                this::class.java,
                COMPONENT_ID,
                "auto-horizontal",
                "Auto Horizontal",
                "Auto-sized horizontal slides."
            )
        private var VARIANT_AUTO_VERTICAL =
            PaletteEntry(
                this::class.java,
                COMPONENT_ID,
                "auto-vertical",
                "Auto Vertical",
                "Auto-sized vertical slides."
            )
        private var VARIANT_FULL_HORIZONTAL =
            PaletteEntry(
                this::class.java,
                COMPONENT_ID,
                "full-horizontal",
                "Full Horizontal",
                "Full-sized horizontal slides."
            )
        private var VARIANT_FULL_VERTICAL =
            PaletteEntry(
                this::class.java,
                COMPONENT_ID,
                "full-vertical",
                "Full Vertical",
                "Full-sized vertical slides."
            )

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
