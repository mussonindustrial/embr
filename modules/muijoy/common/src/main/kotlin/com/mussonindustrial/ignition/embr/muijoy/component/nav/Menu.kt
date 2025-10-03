package com.mussonindustrial.ignition.embr.muijoy.component.nav

import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.embr.perspective.common.component.PerspectiveComponent
import com.mussonindustrial.embr.perspective.common.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.muijoy.Meta
import com.mussonindustrial.ignition.embr.muijoy.MuiJoyComponents

class Menu {
    companion object : PerspectiveComponent {
        override val id: String = "embr.muijoy.nav.menu"

        private val VARIANT_BASE = PaletteEntry(this::class.java, id, "base", "Menu", "")

        override val descriptor: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("Mui Joy Navigation")
                .setId(id)
                .setModuleId(Meta.MODULE_ID)
                .setSchema(schema)
                .setName("Menu")
                .addPaletteEntry(VARIANT_BASE)
                .setDefaultMetaName("Menu")
                .setResources(MuiJoyComponents.BROWSER_RESOURCES)
                .build()
    }
}
