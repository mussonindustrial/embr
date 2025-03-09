package com.mussonindustrial.ignition.embr.charts.component.chart

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.embr.perspective.common.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.charts.Components
import com.mussonindustrial.ignition.embr.charts.Meta.MODULE_ID

class ApexCharts {
    companion object {
        var COMPONENT_ID: String = "embr.chart.apex-charts"
        var SCHEMA: JsonSchema =
            JsonSchema.parse(
                Components::class
                    .java
                    .getResourceAsStream("/schemas/components/embr.chart.apex-charts/props.json")
            )

        private var VARIANT_BASE =
            PaletteEntry(
                this::class.java,
                COMPONENT_ID,
                "base",
                "ApexCharts",
                "Modern & Interactive Open-source Charts",
            )

        var DESCRIPTOR: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("chart")
                .setId(COMPONENT_ID)
                .setModuleId(MODULE_ID)
                .setSchema(SCHEMA)
                .setName("ApexCharts Chart")
                .addPaletteEntry(VARIANT_BASE)
                .setDefaultMetaName("ApexCharts")
                .setResources(Components.BROWSER_RESOURCES)
                .build()
    }
}
