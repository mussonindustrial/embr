package com.mussonindustrial.ignition.embr.periscope.component.embedding

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.ignition.embr.periscope.Components
import com.mussonindustrial.ignition.embr.periscope.Meta.MODULE_ID
import com.mussonindustrial.ignition.embr.periscope.component.PaletteEntry
import com.mussonindustrial.ignition.embr.periscope.component.addPaletteEntry

class AdvancedFlexRepeater {
    companion object {
        var COMPONENT_ID: String = "embr.periscope.embedding.advanced-flex-repeater"
        var SCHEMA: JsonSchema =
            JsonSchema.parse(
                Components::class
                    .java
                    .getResourceAsStream(
                        "/schemas/components/embr.periscope.embedding.advanced-flex-repeater/props.json"
                    )
            )

        private var VARIANT_BASE =
            PaletteEntry(
                COMPONENT_ID,
                "base",
                "Flex Repeater +",
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
                .setDefaultMetaName("AdvancedFlexRepeater")
                .setResources(Components.BROWSER_RESOURCES)
                .build()
    }
}