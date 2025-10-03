package com.mussonindustrial.ignition.embr.muijoy.component.input

import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.embr.perspective.common.component.PerspectiveComponent
import com.mussonindustrial.embr.perspective.common.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.muijoy.Meta
import com.mussonindustrial.ignition.embr.muijoy.MuiJoyComponents

class Slider {
    companion object : PerspectiveComponent {
        override val id: String = "embr.muijoy.input.slider"

        private val VARIANT_BASE = PaletteEntry(this::class.java, id, "base", "Slider", "")

        override val descriptor: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("Mui Joy Input")
                .setId(id)
                .setModuleId(Meta.MODULE_ID)
                .setSchema(schema)
                .setName("Slider")
                .addPaletteEntry(VARIANT_BASE)
                .setDefaultMetaName("Slider")
                .setResources(MuiJoyComponents.BROWSER_RESOURCES)
                .build()
    }
}
