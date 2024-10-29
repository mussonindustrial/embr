package com.mussonindustrial.ignition.embr.periscope.component.embedding

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.embr.perspective.common.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.periscope.Components
import com.mussonindustrial.ignition.embr.periscope.Meta.MODULE_ID

class FlexRepeater {
    companion object {
        var COMPONENT_ID: String = "embr.periscope.embedding.flex-repeater"
        var SCHEMA: JsonSchema =
            JsonSchema.parse(
                Components::class
                    .java
                    .getResourceAsStream(
                        "/schemas/components/embr.periscope.embedding.flex-repeater/props.json"
                    )
            )

        private var VARIANT_BASE =
            PaletteEntry(
                this::class.java,
                COMPONENT_ID,
                "base",
                "Flex Repeater +",
                "Creates multiple instances of views for display in another view."
            )

        private var VARIANT_ROW =
            PaletteEntry(
                this::class.java,
                COMPONENT_ID,
                "row",
                "Row",
                "Creates multiple instances of views for display in another view."
            )

        private var VARIANT_COLUMN =
            PaletteEntry(
                this::class.java,
                COMPONENT_ID,
                "column",
                "Column",
                "Creates multiple instances of views for display in another view."
            )

        var DESCRIPTOR: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("embedding")
                .setId(COMPONENT_ID)
                .setModuleId(MODULE_ID)
                .setSchema(SCHEMA)
                .setName("Flex Repeater +")
                .addPaletteEntry(VARIANT_BASE)
                .addPaletteEntry(VARIANT_ROW)
                .addPaletteEntry(VARIANT_COLUMN)
                .setDefaultMetaName("FlexRepeaterPlus")
                .setResources(Components.BROWSER_RESOURCES)
                .build()
    }
}
