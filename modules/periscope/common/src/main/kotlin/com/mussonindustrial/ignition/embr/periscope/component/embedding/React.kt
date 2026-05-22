package com.mussonindustrial.ignition.embr.periscope.component.embedding

import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.embr.perspective.common.component.PerspectiveComponent
import com.mussonindustrial.embr.perspective.common.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.periscope.Meta.MODULE_ID
import com.mussonindustrial.ignition.embr.periscope.PeriscopeComponents

class React {
    companion object : PerspectiveComponent {
        override val id: String = "embr.periscope.embedding.react"

        private val VARIANT_BASE =
            PaletteEntry(this::class.java, id, "base", "React", "React component.")

        override val descriptor: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("Embedding +")
                .setId(id)
                .setModuleId(MODULE_ID)
                .setSchema(schema)
                .setName("React")
                .addPaletteEntry(VARIANT_BASE)
                .setDefaultMetaName("React")
                .setResources(PeriscopeComponents.BROWSER_RESOURCES)
                .build()
    }
}
