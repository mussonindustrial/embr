package com.mussonindustrial.ignition.embr.charts.component.chart

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.inductiveautomation.perspective.common.api.ComponentEventDescriptor
import com.mussonindustrial.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.embr.perspective.common.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.charts.Components
import com.mussonindustrial.ignition.embr.charts.Meta.MODULE_ID

class SmoothieChart {
    companion object {
        var COMPONENT_ID: String = "embr.chart.smoothie-chart"
        var SCHEMA: JsonSchema =
            JsonSchema.parse(
                Components::class
                    .java
                    .getResourceAsStream("/schemas/components/${COMPONENT_ID}/props.json")
            )

        var EVENTS =
            listOf(
                ComponentEventDescriptor(
                    "getChartData",
                    "Testing",
                    JsonSchema.parse(
                        Components::class
                            .java
                            .getResourceAsStream(
                                "/schemas/components/${COMPONENT_ID}/events/getChartData.json"
                            )
                    )
                )
            )

        private var VARIANT_BASE =
            PaletteEntry(
                this::class.java,
                COMPONENT_ID,
                "base",
                "SmoothieChart",
                "Smoothie Charts is a simple library for displaying smooth live time lines. "
            )

        var DESCRIPTOR: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("chart")
                .setId(COMPONENT_ID)
                .setModuleId(MODULE_ID)
                .setSchema(SCHEMA)
                .setEvents(EVENTS)
                .setName("SmoothieChart")
                .addPaletteEntry(VARIANT_BASE)
                .setDefaultMetaName("SmoothieChart")
                .setResources(Components.BROWSER_RESOURCES)
                .build()
    }
}
