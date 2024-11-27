package com.mussonindustrial.ignition.embr.periscope.component.container

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.embr.perspective.common.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.periscope.Components
import com.mussonindustrial.ignition.embr.periscope.Meta.MODULE_ID

class CoordinateCanvas {
    companion object {
        var COMPONENT_ID: String = "embr.periscope.container.coordinate-canvas"
        var SCHEMA: JsonSchema =
            JsonSchema.parse(
                Components::class
                    .java
                    .getResourceAsStream(
                        "/schemas/components/embr.periscope.container.coordinate-canvas/props.json"
                    )
            )

        private var VARIANT_BASE =
            PaletteEntry(
                this::class.java,
                COMPONENT_ID,
                "base",
                "Coordinate Canvas",
                "Coordinate container with pan and zoom capabilities."
            )

        var DESCRIPTOR: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("Container +")
                .setId(COMPONENT_ID)
                .setModuleId(MODULE_ID)
                .setSchema(SCHEMA)
                .setName("Coordinate Canvas")
                .addPaletteEntry(VARIANT_BASE)
                .setDefaultMetaName("CoordinateCanvas")
                .setResources(Components.BROWSER_RESOURCES)
                .build()
    }
}
