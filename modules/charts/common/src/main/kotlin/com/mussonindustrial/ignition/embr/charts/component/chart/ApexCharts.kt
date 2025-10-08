package com.mussonindustrial.ignition.embr.charts.component.chart

import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.embr.perspective.common.component.PerspectiveComponent
import com.mussonindustrial.embr.perspective.common.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.charts.Components
import com.mussonindustrial.ignition.embr.charts.Meta.MODULE_ID

class ApexCharts {
    companion object : PerspectiveComponent {
        override val id: String = "embr.chart.apex-charts"

        private val VARIANT_BASE =
            PaletteEntry(
                this::class.java,
                id,
                "base",
                "ApexCharts",
                "Modern & Interactive Open-source Charts",
            )

        private val VARIANT_AREA =
            PaletteEntry(
                this::class.java,
                id,
                "area",
                "Area",
                "Displays data as filled areas under a line to show trends over time.",
            )
        private val VARIANT_BAR =
            PaletteEntry(
                this::class.java,
                id,
                "bar",
                "Bar",
                "Compares values across categories using horizontal bars.",
            )
        private val VARIANT_BOXPLOT =
            PaletteEntry(
                this::class.java,
                id,
                "boxplot",
                "Box Plot",
                "Visualizes statistical distribution with quartiles and outliers.",
            )
        private val VARIANT_BUBBLE =
            PaletteEntry(
                this::class.java,
                id,
                "bubble",
                "Bubble",
                "Shows data with three dimensions using bubble size and position.",
            )
        private val VARIANT_CANDLESTICK =
            PaletteEntry(
                this::class.java,
                id,
                "candlestick",
                "Candlestick",
                "Used in finance to show open, high, low, and close values.",
            )
        private val VARIANT_COLUMN =
            PaletteEntry(
                this::class.java,
                id,
                "column",
                "Column",
                "Compares values across categories using vertical bars.",
            )
        private val VARIANT_FUNNEL =
            PaletteEntry(
                this::class.java,
                id,
                "funnel",
                "Funnel",
                "Displays stages in a process with decreasing proportions.",
            )
        private val VARIANT_HEATMAP =
            PaletteEntry(
                this::class.java,
                id,
                "heatmap",
                "Heat Map",
                "Represents data intensity using color gradients in a grid.",
            )
        private val VARIANT_LINE =
            PaletteEntry(
                this::class.java,
                id,
                "line",
                "Line",
                "Connects data points with straight lines to show trends over time.",
            )
        private val VARIANT_MIXED =
            PaletteEntry(
                this::class.java,
                id,
                "mixed",
                "Mixed",
                "Combines multiple chart types in a single view.",
            )
        private val VARIANT_PIE =
            PaletteEntry(
                this::class.java,
                id,
                "pie",
                "Pie",
                "Shows parts of a whole as proportional slices of a circle.",
            )
        private val VARIANT_POLARAREA =
            PaletteEntry(
                this::class.java,
                id,
                "polararea",
                "Polar Area",
                "Displays values as segments radiating from the center.",
            )
        private val VARIANT_RADAR =
            PaletteEntry(
                this::class.java,
                id,
                "radar",
                "Radar",
                "Shows multivariate data on axes starting from the same point.",
            )
        private val VARIANT_RADIALBAR =
            PaletteEntry(
                this::class.java,
                id,
                "radialbar",
                "Radial Bar",
                "Displays progress or values using circular bars.",
            )
        private val VARIANT_RANGEAREA =
            PaletteEntry(
                this::class.java,
                id,
                "rangearea",
                "Range Area",
                "Shows a range between two values over a time period.",
            )
        private val VARIANT_SCATTER =
            PaletteEntry(
                this::class.java,
                id,
                "scatter",
                "Scatter",
                "Plots individual points to reveal relationships or clusters.",
            )
        private val VARIANT_SLOPE =
            PaletteEntry(
                this::class.java,
                id,
                "slope",
                "Slope",
                "Highlights changes between two time points with connecting lines.",
            )
        private val VARIANT_TREEMAP =
            PaletteEntry(
                this::class.java,
                id,
                "treemap",
                "Tree Map",
                "Uses nested rectangles to show part-to-whole relationships.",
            )

        override val descriptor: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("chart")
                .setId(id)
                .setModuleId(MODULE_ID)
                .setSchema(schema)
                .setName("ApexCharts Chart")
                .addPaletteEntry(VARIANT_BASE)
                .addPaletteEntry(VARIANT_AREA)
                .addPaletteEntry(VARIANT_BAR)
                .addPaletteEntry(VARIANT_BOXPLOT)
                .addPaletteEntry(VARIANT_BUBBLE)
                .addPaletteEntry(VARIANT_CANDLESTICK)
                .addPaletteEntry(VARIANT_COLUMN)
                .addPaletteEntry(VARIANT_FUNNEL)
                .addPaletteEntry(VARIANT_HEATMAP)
                .addPaletteEntry(VARIANT_LINE)
                .addPaletteEntry(VARIANT_MIXED)
                .addPaletteEntry(VARIANT_PIE)
                .addPaletteEntry(VARIANT_POLARAREA)
                .addPaletteEntry(VARIANT_RADAR)
                .addPaletteEntry(VARIANT_RADIALBAR)
                .addPaletteEntry(VARIANT_RANGEAREA)
                .addPaletteEntry(VARIANT_SCATTER)
                .addPaletteEntry(VARIANT_SLOPE)
                .addPaletteEntry(VARIANT_TREEMAP)
                .setDefaultMetaName("ApexCharts")
                .setResources(Components.BROWSER_RESOURCES)
                .build()
    }
}
