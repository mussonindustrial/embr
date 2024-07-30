package com.mussonindustrial.ignition.embr.charts.component.chart

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.ignition.embr.charts.Components
import com.mussonindustrial.ignition.embr.common.Embr
import com.mussonindustrial.ignition.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.ignition.embr.perspective.common.component.addPaletteEntry

class ChartJs {
    companion object {
        val id: String = "embr.chart.chart-js"
        val schema: JsonSchema = JsonSchema.parse(Components::class.java.getResourceAsStream("/chart-js.props.json"))

        private val variantBase =
            PaletteEntry(
                Components::class.java,
                "chart-js.base",
                "Chart.js",
                "A simple yet flexible JavaScript charting library for the modern web.",
            )
        private val variantBar =
            PaletteEntry(
                Components::class.java,
                "chart-js.bar",
                "Bar",
                "A bar chart or bar graph is a chart or graph that presents categorical data with rectangular bars with heights or lengths proportional to the values that they represent. The bars can be plotted vertically or horizontally.",
            )
        private val variantBubble =
            PaletteEntry(
                Components::class.java,
                "chart-js.bubble",
                "Bubble",
                "A bubble chart (aka bubble plot) is an extension of the scatter plot used to look at relationships between three numeric variables. Each dot in a bubble chart corresponds with a single data point, and the variables' values for each point are indicated by horizontal position, vertical position, and dot size.",
            )
        private val variantBoxplot =
            PaletteEntry(
                Components::class.java,
                "chart-js.boxplot",
                "Box Plot",
                "Box Plot is a graphical method to visualize data distribution for gaining insights and making informed decisions. Box plot is a type of chart that depicts a group of numerical data through their quartiles.",
            )
        private val variantDoughnut =
            PaletteEntry(
                Components::class.java,
                "chart-js.doughnut",
                "Doughnut",
                "A doughnut chart is a circular statistical graphic which is divided into slices to illustrate numerical proportion. In a pie chart, the arc length of each slice is proportional to the quantity it represents.",
            )
        private val variantLine =
            PaletteEntry(
                Components::class.java,
                "chart-js.line",
                "Line",
                "A line chart or line graph, also known as curve chart, is a type of chart that displays information as a series of data points called 'markers' connected by straight line segments.",
            )
        private val variantPie =
            PaletteEntry(
                Components::class.java,
                "chart-js.pie",
                "Pie",
                "A pie chart is a circular statistical graphic which is divided into slices to illustrate numerical proportion. In a pie chart, the arc length of each slice is proportional to the quantity it represents.",
            )
        private val variantPolarArea =
            PaletteEntry(
                Components::class.java,
                "chart-js.polararea",
                "Polar Area",
                "Polar area charts are similar to pie charts, but each segment has the same angle - the radius of the segment differs depending on the value.",
            )
        private val variantRadar =
            PaletteEntry(
                Components::class.java,
                "chart-js.radar",
                "Radar",
                "A radar chart is a graphical method of displaying multivariate data in the form of a two-dimensional chart of three or more quantitative variables represented on axes starting from the same point.",
            )
        private val variantVoilin =
            PaletteEntry(
                Components::class.java,
                "chart-js.violin",
                "Violin",
                "A violin plot depicts distributions of numeric data for one or more groups using density curves. The width of each curve corresponds with the approximate frequency of data points in each region.",
            )

        var descriptor: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("chart")
                .setId(id)
                .setModuleId(Embr.CHARTS.id)
                .setSchema(schema)
                .setName("Chart.js Chart")
                .addPaletteEntry(variantBase)
                .addPaletteEntry(variantBar)
                .addPaletteEntry(variantBubble)
                .addPaletteEntry(variantBoxplot)
                .addPaletteEntry(variantDoughnut)
                .addPaletteEntry(variantLine)
                .addPaletteEntry(variantPie)
                .addPaletteEntry(variantPolarArea)
                .addPaletteEntry(variantRadar)
                .addPaletteEntry(variantVoilin)
                .setDefaultMetaName("Chartjs")
                .setResources(Components.BROWSER_RESOURCES)
                .build()
    }
}
