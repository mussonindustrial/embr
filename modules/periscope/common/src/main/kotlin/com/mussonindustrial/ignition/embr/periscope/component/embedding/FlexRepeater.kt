package com.mussonindustrial.ignition.embr.periscope.component.embedding

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.embr.perspective.common.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.periscope.Meta.MODULE_ID
import com.mussonindustrial.ignition.embr.periscope.PeriscopeComponents

class FlexRepeater {
    companion object {
        var COMPONENT_ID: String = "embr.periscope.embedding.flex-repeater"
        var SCHEMA: JsonSchema =
            JsonSchema.parse(
                PeriscopeComponents::class
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
                "Creates multiple instances of views for display in another view. View props are handled server-side for decreased latency."
            )

        private var VARIANT_ROW =
            PaletteEntry(
                this::class.java,
                COMPONENT_ID,
                "row",
                "Row",
                "Creates multiple instances of views for display in another view. View props are handled server-side for decreased latency."
            )

        private var VARIANT_COLUMN =
            PaletteEntry(
                this::class.java,
                COMPONENT_ID,
                "column",
                "Column",
                "Creates multiple instances of views for display in another view. View props are handled server-side for decreased latency."
            )

        var DESCRIPTOR: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("Embedding +")
                .setId(COMPONENT_ID)
                .setModuleId(MODULE_ID)
                .setSchema(SCHEMA)
                .setName("Flex Repeater +")
                .addPaletteEntry(VARIANT_BASE)
                .addPaletteEntry(VARIANT_ROW)
                .addPaletteEntry(VARIANT_COLUMN)
                .setDefaultMetaName("FlexRepeaterPlus")
                .setResources(PeriscopeComponents.BROWSER_RESOURCES)
                .build()
    }
}
