package com.mussonindustrial.ignition.embr.periscope.component.embedding

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.embr.perspective.common.component.PerspectiveComponent
import com.mussonindustrial.embr.perspective.common.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.periscope.Meta.MODULE_ID
import com.mussonindustrial.ignition.embr.periscope.PeriscopeComponents

class FlexRepeater {
    companion object : PerspectiveComponent {
        override val id: String = "embr.periscope.embedding.flex-repeater"
        override val schema: JsonSchema =
            JsonSchema.parse(
                PeriscopeComponents::class
                    .java
                    .getResourceAsStream(
                        "/schemas/components/embr.periscope.embedding.flex-repeater/props.json"
                    )
            )

        private val VARIANT_BASE =
            PaletteEntry(
                this::class.java,
                id,
                "base",
                "Flex Repeater +",
                "Creates multiple instances of views for display in another view. View props are handled server-side for decreased latency.",
            )

        private val VARIANT_ROW =
            PaletteEntry(
                this::class.java,
                id,
                "row",
                "Row",
                "Creates multiple instances of views for display in another view. View props are handled server-side for decreased latency.",
            )

        private val VARIANT_COLUMN =
            PaletteEntry(
                this::class.java,
                id,
                "column",
                "Column",
                "Creates multiple instances of views for display in another view. View props are handled server-side for decreased latency.",
            )

        override val descriptor: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("Embedding +")
                .setId(id)
                .setModuleId(MODULE_ID)
                .setSchema(schema)
                .setName("Flex Repeater +")
                .addPaletteEntry(VARIANT_BASE)
                .addPaletteEntry(VARIANT_ROW)
                .addPaletteEntry(VARIANT_COLUMN)
                .setDefaultMetaName("FlexRepeaterPlus")
                .setResources(PeriscopeComponents.BROWSER_RESOURCES)
                .build()
    }
}
