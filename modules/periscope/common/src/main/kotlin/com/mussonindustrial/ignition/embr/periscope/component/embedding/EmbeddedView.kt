package com.mussonindustrial.ignition.embr.periscope.component.embedding

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.embr.perspective.common.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.periscope.Components
import com.mussonindustrial.ignition.embr.periscope.Meta.MODULE_ID

class EmbeddedView {
    companion object {
        var COMPONENT_ID: String = "embr.periscope.embedding.view"
        var SCHEMA: JsonSchema =
            JsonSchema.parse(
                Components::class
                    .java
                    .getResourceAsStream(
                        "/schemas/components/embr.periscope.embedding.view/props.json"
                    )
            )

        private var VARIANT_BASE =
            PaletteEntry(
                this::class.java,
                COMPONENT_ID,
                "base",
                "Embedded View +",
                "Enables an entire view to be embedded within another view. View props are handled server-side for decreased latency."
            )

        var DESCRIPTOR: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("Embedding +")
                .setId(COMPONENT_ID)
                .setModuleId(MODULE_ID)
                .setSchema(SCHEMA)
                .setName("Embedded View +")
                .addPaletteEntry(VARIANT_BASE)
                .setDefaultMetaName("EmbeddedViewPlus")
                .setResources(Components.BROWSER_RESOURCES)
                .build()
    }
}
