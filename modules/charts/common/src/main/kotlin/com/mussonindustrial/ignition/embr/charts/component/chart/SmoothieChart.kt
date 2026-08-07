package com.mussonindustrial.ignition.embr.charts.component.chart

import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.embr.perspective.common.component.PerspectiveComponent
import com.mussonindustrial.embr.perspective.common.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.charts.Components
import com.mussonindustrial.ignition.embr.charts.Meta.MODULE_ID

class SmoothieChart {
    companion object : PerspectiveComponent {
        override val id: String = "embr.chart.smoothie-chart"

        private var VARIANT_BASE =
            PaletteEntry(
                this::class.java,
                id,
                "base",
                "Smoothie Chart",
                "Smoothie Charts is a simple library for displaying smooth live time lines. ",
            )

        override val descriptor: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("chart")
                .setId(id)
                .setModuleId(MODULE_ID)
                .setSchema(schema)
                .setName("Smoothie Chart")
                .addPaletteEntry(VARIANT_BASE)
                .setDefaultMetaName("SmoothieChart")
                .setResources(Components.BROWSER_RESOURCES)
                .build()
    }
}
