package com.mussonindustrial.ignition.embr.muijoy.component.embedding

import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.embr.perspective.common.component.PerspectiveComponent
import com.mussonindustrial.embr.perspective.common.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.muijoy.Meta.MODULE_ID
import com.mussonindustrial.ignition.embr.muijoy.MuiJoyComponents

class Swiper {
    companion object : PerspectiveComponent {
        override val id: String = "embr.muijoy.embedding.swiper"

        private val VARIANT_BASE =
            PaletteEntry(
                this::class.java,
                id,
                "base",
                "Swiper",
                "The Most Modern Mobile Touch Slider.",
            )
        private val VARIANT_AUTO_HORIZONTAL =
            PaletteEntry(
                this::class.java,
                id,
                "auto-horizontal",
                "Auto Horizontal",
                "Auto-sized horizontal slides.",
            )
        private val VARIANT_AUTO_VERTICAL =
            PaletteEntry(
                this::class.java,
                id,
                "auto-vertical",
                "Auto Vertical",
                "Auto-sized vertical slides.",
            )
        private val VARIANT_FULL_HORIZONTAL =
            PaletteEntry(
                this::class.java,
                id,
                "full-horizontal",
                "Full Horizontal",
                "Full-sized horizontal slides.",
            )
        private val VARIANT_FULL_VERTICAL =
            PaletteEntry(
                this::class.java,
                id,
                "full-vertical",
                "Full Vertical",
                "Full-sized vertical slides.",
            )

        override val descriptor: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("Embedding +")
                .setId(id)
                .setModuleId(MODULE_ID)
                .setSchema(schema)
                .setName("Swiper")
                .addPaletteEntry(VARIANT_BASE)
                .addPaletteEntry(VARIANT_AUTO_HORIZONTAL)
                .addPaletteEntry(VARIANT_AUTO_VERTICAL)
                .addPaletteEntry(VARIANT_FULL_HORIZONTAL)
                .addPaletteEntry(VARIANT_FULL_VERTICAL)
                .setDefaultMetaName("Swiper")
                .setResources(MuiJoyComponents.BROWSER_RESOURCES)
                .build()
    }
}
