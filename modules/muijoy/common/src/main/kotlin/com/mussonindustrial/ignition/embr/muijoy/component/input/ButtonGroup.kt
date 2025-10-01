package com.mussonindustrial.ignition.embr.muijoy.component.input

import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.embr.perspective.common.component.PerspectiveComponent
import com.mussonindustrial.embr.perspective.common.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.muijoy.Meta
import com.mussonindustrial.ignition.embr.muijoy.MuiJoyComponents

class ButtonGroup {
    companion object : PerspectiveComponent {
        override val id: String = "embr.muijoy.input.button-group"

        private val VARIANT_BASE =
            PaletteEntry(
                this::class.java,
                id,
                "base",
                "Button Group",
                "The Button Group combines a set of related buttons.",
            )

        override val descriptor: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("MUI Joy")
                .setId(id)
                .setModuleId(Meta.MODULE_ID)
                .setSchema(schema)
                .setName("Button Group")
                .addPaletteEntry(VARIANT_BASE)
                .setDefaultMetaName("ButtonGroup")
                .setResources(MuiJoyComponents.BROWSER_RESOURCES)
                .build()
    }
}
