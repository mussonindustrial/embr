package com.mussonindustrial.ignition.embr.periscope.component.embedding

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.embr.perspective.common.component.PerspectiveComponent
import com.mussonindustrial.embr.perspective.common.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.periscope.Meta.MODULE_ID
import com.mussonindustrial.ignition.embr.periscope.PeriscopeComponents

class EmbeddedView {
    companion object : PerspectiveComponent {
        override val id: String = "embr.periscope.embedding.view"
        override val schema: JsonSchema =
            JsonSchema.parse(
                PeriscopeComponents::class
                    .java
                    .getResourceAsStream(
                        "/schemas/components/embr.periscope.embedding.view/props.json"
                    )
            )

        private val VARIANT_BASE =
            PaletteEntry(
                this::class.java,
                id,
                "base",
                "Embedded View +",
                "Enables an entire view to be embedded within another view. View props are handled server-side for decreased latency.",
            )

        override val descriptor: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("Embedding +")
                .setId(id)
                .setModuleId(MODULE_ID)
                .setSchema(schema)
                .setName("Embedded View +")
                .addPaletteEntry(VARIANT_BASE)
                .setDefaultMetaName("EmbeddedViewPlus")
                .setResources(PeriscopeComponents.BROWSER_RESOURCES)
                .build()
    }
}
