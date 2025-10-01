package com.mussonindustrial.ignition.embr.muijoy.component.embedding

import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.embr.perspective.common.component.PerspectiveComponent
import com.mussonindustrial.embr.perspective.common.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.muijoy.Meta.MODULE_ID
import com.mussonindustrial.ignition.embr.muijoy.MuiJoyComponents

class JsonView {
    companion object : PerspectiveComponent {
        override val id: String = "embr.muijoy.embedding.json-view"

        private val VARIANT_BASE =
            PaletteEntry(
                this::class.java,
                id,
                "base",
                "Json View",
                "Renders a view from its Json representation.",
            )

        override val descriptor: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("Embedding +")
                .setId(id)
                .setModuleId(MODULE_ID)
                .setSchema(schema)
                .setName("Json View")
                .addPaletteEntry(VARIANT_BASE)
                .setDefaultMetaName("JsonView")
                .setResources(MuiJoyComponents.BROWSER_RESOURCES)
                .build()
    }
}
