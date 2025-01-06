package com.mussonindustrial.ignition.embr.charts.component.chart

import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.embr.perspective.common.component.ComponentSchemaLoader
import com.mussonindustrial.embr.perspective.common.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.charts.Components
import com.mussonindustrial.ignition.embr.charts.Meta.MODULE_ID

class SmoothieChart {
    companion object {
        var COMPONENT_ID: String = "embr.chart.smoothie-chart"

        private val schemaLoader = ComponentSchemaLoader(Components::class.java, COMPONENT_ID)
        private val SCHEMA = schemaLoader.getSchema()
        private val EVENTS =
            listOf(
                schemaLoader.getEventDescriptor(
                    "onChartUpdate",
                    "Called on an interval configured in the component's properties."
                )
            )

        private var VARIANT_BASE =
            schemaLoader.getPaletteEntry(
                "base",
                "Smoothie Chart",
                "Smoothie Charts is a simple library for displaying smooth live time lines. "
            )

        var DESCRIPTOR: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("chart")
                .setId(COMPONENT_ID)
                .setModuleId(MODULE_ID)
                .setSchema(SCHEMA)
                .setEvents(EVENTS)
                .setName("Smoothie Chart")
                .addPaletteEntry(VARIANT_BASE)
                .setDefaultMetaName("SmoothieChart")
                .setResources(Components.BROWSER_RESOURCES)
                .build()
    }
}
