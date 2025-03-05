package com.mussonindustrial.ignition.embr.periscope.component.container

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.embr.perspective.common.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.periscope.Meta.MODULE_ID
import com.mussonindustrial.ignition.embr.periscope.PeriscopeComponents

class CoordinateCanvas {
    companion object {
        var COMPONENT_ID: String = "embr.periscope.container.coordinate-canvas"
        var SCHEMA: JsonSchema =
            JsonSchema.parse(
                PeriscopeComponents::class
                    .java
                    .getResourceAsStream("/schemas/components/${COMPONENT_ID}/props.json")
            )
        var CHILD_POSITION_SCHEMA: JsonSchema =
            JsonSchema.parse(
                PeriscopeComponents::class
                    .java
                    .getResourceAsStream("/schemas/components/${COMPONENT_ID}/children.json")
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
                .setChildPositionSchema(CHILD_POSITION_SCHEMA)
                .setName("Coordinate Canvas")
                .addPaletteEntry(VARIANT_BASE)
                .setDefaultMetaName("CoordinateCanvas")
                .setResources(PeriscopeComponents.BROWSER_RESOURCES)
                .build()
    }
}
