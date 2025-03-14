package com.mussonindustrial.ignition.embr.periscope.component.container

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.embr.perspective.common.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.periscope.Meta.MODULE_ID
import com.mussonindustrial.ignition.embr.periscope.PeriscopeComponents

class Portal {
    companion object {
        var COMPONENT_ID: String = "embr.periscope.container.portal"
        var SCHEMA: JsonSchema =
            JsonSchema.parse(
                PeriscopeComponents::class
                    .java
                    .getResourceAsStream(
                        "/schemas/components/embr.periscope.container.portal/props.json"
                    )
            )

        private var VARIANT_BASE =
            PaletteEntry(this::class.java, COMPONENT_ID, "base", "Portal", "Portal.")

        var DESCRIPTOR: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("Container +")
                .setId(COMPONENT_ID)
                .setModuleId(MODULE_ID)
                .setSchema(SCHEMA)
                .setName("Portal")
                .addPaletteEntry(VARIANT_BASE)
                .setDefaultMetaName("Portal")
                .setResources(PeriscopeComponents.BROWSER_RESOURCES)
                .build()
    }
}
