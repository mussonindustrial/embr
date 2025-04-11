package com.mussonindustrial.ignition.embr.charts.component.chart

import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.embr.perspective.common.component.PerspectiveComponent
import com.mussonindustrial.embr.perspective.common.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.charts.Components
import com.mussonindustrial.ignition.embr.charts.Meta.MODULE_ID

class ChartJs {
    companion object : PerspectiveComponent {
        override val id: String = "embr.chart.chart-js"

        private val VARIANT_BASE =
            PaletteEntry(
                this::class.java,
                id,
                "base",
                "Chart.js",
                "A simple yet flexible JavaScript charting library for the modern web.",
            )
        private val VARIANT_BAR =
            PaletteEntry(
                this::class.java,
                id,
                "bar",
                "Bar",
                "A bar chart or bar graph is a chart or graph that presents categorical data with rectangular bars with heights or lengths proportional to the values that they represent. The bars can be plotted vertically or horizontally.",
            )
        private val VARIANT_BUBBLE =
            PaletteEntry(
                this::class.java,
                id,
                "bubble",
                "Bubble",
                "A bubble chart (aka bubble plot) is an extension of the scatter plot used to look at relationships between three numeric variables. Each dot in a bubble chart corresponds with a single data point, and the variables' values for each point are indicated by horizontal position, vertical position, and dot size.",
            )
        private val VARIANT_BOXPLOT =
            PaletteEntry(
                this::class.java,
                id,
                "boxplot",
                "Box Plot",
                "Box Plot is a graphical method to visualize data distribution for gaining insights and making informed decisions. Box plot is a type of chart that depicts a group of numerical data through their quartiles.",
            )
        private val VARIANT_DOUGHNUT =
            PaletteEntry(
                this::class.java,
                id,
                "doughnut",
                "Doughnut",
                "A doughnut chart is a circular statistical graphic which is divided into slices to illustrate numerical proportion. In a pie chart, the arc length of each slice is proportional to the quantity it represents.",
            )
        private val VARIANT_LINE =
            PaletteEntry(
                this::class.java,
                id,
                "line",
                "Line",
                "A line chart or line graph, also known as curve chart, is a type of chart that displays information as a series of data points called 'markers' connected by straight line segments.",
            )
        private val VARIANT_PIE =
            PaletteEntry(
                this::class.java,
                id,
                "pie",
                "Pie",
                "A pie chart is a circular statistical graphic which is divided into slices to illustrate numerical proportion. In a pie chart, the arc length of each slice is proportional to the quantity it represents.",
            )
        private val VARIANT_POLARAREA =
            PaletteEntry(
                this::class.java,
                id,
                "polararea",
                "Polar Area",
                "Polar area charts are similar to pie charts, but each segment has the same angle - the radius of the segment differs depending on the value.",
            )
        private val VARIANT_RADAR =
            PaletteEntry(
                this::class.java,
                id,
                "radar",
                "Radar",
                "A radar chart is a graphical method of displaying multivariate data in the form of a two-dimensional chart of three or more quantitative variables represented on axes starting from the same point.",
            )
        private val VARIANT_VIOLIN =
            PaletteEntry(
                this::class.java,
                id,
                "violin",
                "Violin",
                "A violin plot depicts distributions of numeric data for one or more groups using density curves. The width of each curve corresponds with the approximate frequency of data points in each region.",
            )

        override val descriptor: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("chart")
                .setId(id)
                .setModuleId(MODULE_ID)
                .setSchema(schema)
                .setName("Chart.js Chart")
                .addPaletteEntry(VARIANT_BASE)
                .addPaletteEntry(VARIANT_BAR)
                .addPaletteEntry(VARIANT_BUBBLE)
                .addPaletteEntry(VARIANT_BOXPLOT)
                .addPaletteEntry(VARIANT_DOUGHNUT)
                .addPaletteEntry(VARIANT_LINE)
                .addPaletteEntry(VARIANT_PIE)
                .addPaletteEntry(VARIANT_POLARAREA)
                .addPaletteEntry(VARIANT_RADAR)
                .addPaletteEntry(VARIANT_VIOLIN)
                .setDefaultMetaName("Chartjs")
                .setResources(Components.BROWSER_RESOURCES)
                .build()
    }
}
