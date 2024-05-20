package com.mussonindustrial.ignition.embr.charts.component.chart

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.mussonindustrial.ignition.embr.charts.Components
import com.mussonindustrial.ignition.embr.charts.Meta.MODULE_ID
import com.mussonindustrial.ignition.embr.charts.component.PaletteEntry
import com.mussonindustrial.ignition.embr.charts.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.charts.component.getIcon
import com.mussonindustrial.ignition.embr.charts.component.getJsonProps

class ChartJs {
    companion object {
        var COMPONENT_ID: String = "embr.chart.chart-js"
        var SCHEMA: JsonSchema = JsonSchema.parse(Components::class.java.getResourceAsStream("/chart-js.props.json"))

        private var VARIANT_BASE = PaletteEntry("",
            "Chart.js",
            "A simple yet flexible JavaScript charting library for the modern web.",
            getIcon("/images/thumbnails/chart-js.base.png"),
            getJsonProps("/variants/chart-js.base.props.json")
        )
        private var VARIANT_BAR = PaletteEntry("chart-js.bar",
            "Bar",
            "A bar chart or bar graph is a chart or graph that presents categorical data with rectangular bars with heights or lengths proportional to the values that they represent. The bars can be plotted vertically or horizontally.",
            getIcon("/images/thumbnails/chart-js.bar.png"),
            getJsonProps("/variants/chart-js.bar.props.json")
        )
        private var VARIANT_BUBBLE = PaletteEntry("chart-js.bubble",
            "Bubble",
            "A bubble chart (aka bubble plot) is an extension of the scatter plot used to look at relationships between three numeric variables. Each dot in a bubble chart corresponds with a single data point, and the variables' values for each point are indicated by horizontal position, vertical position, and dot size.",
            getIcon("/images/thumbnails/chart-js.bubble.png"),
            getJsonProps("/variants/chart-js.bubble.props.json")
        )
        private var VARIANT_BOXPLOT = PaletteEntry("chart-js.boxplot",
            "Box Plot",
            "Box Plot is a graphical method to visualize data distribution for gaining insights and making informed decisions. Box plot is a type of chart that depicts a group of numerical data through their quartiles.",
            getIcon("/images/thumbnails/chart-js.boxplot.png"),
            getJsonProps("/variants/chart-js.boxplot.props.json")
        )
        private var VARIANT_DOUGHNUT = PaletteEntry("chart-js.doughnut",
            "Doughnut",
            "A doughnut chart is a circular statistical graphic which is divided into slices to illustrate numerical proportion. In a pie chart, the arc length of each slice is proportional to the quantity it represents.",
            getIcon("/images/thumbnails/chart-js.doughnut.png"),
            getJsonProps("/variants/chart-js.doughnut.props.json")
        )
        private var VARIANT_LINE = PaletteEntry("chart-js.line",
            "Line",
            "A line chart or line graph, also known as curve chart, is a type of chart that displays information as a series of data points called 'markers' connected by straight line segments.",
            getIcon("/images/thumbnails/chart-js.line.png"),
            getJsonProps("/variants/chart-js.line.props.json")
        )
        private var VARIANT_PIE = PaletteEntry("chart-js.pie",
            "Pie",
            "A pie chart is a circular statistical graphic which is divided into slices to illustrate numerical proportion. In a pie chart, the arc length of each slice is proportional to the quantity it represents.",
            getIcon("/images/thumbnails/chart-js.pie.png"),
            getJsonProps("/variants/chart-js.pie.props.json")
        )
        private var VARIANT_POLARAREA = PaletteEntry("chart-js.polararea",
            "Polar Area",
            "Polar area charts are similar to pie charts, but each segment has the same angle - the radius of the segment differs depending on the value.",
            getIcon("/images/thumbnails/chart-js.polararea.png"),
            getJsonProps("/variants/chart-js.polararea.props.json")
        )
        private var VARIANT_RADAR = PaletteEntry("chart-js.radar",
            "Radar",
            "A radar chart is a graphical method of displaying multivariate data in the form of a two-dimensional chart of three or more quantitative variables represented on axes starting from the same point.",
            getIcon("/images/thumbnails/chart-js.radar.png"),
            getJsonProps("/variants/chart-js.radar.props.json")
        )
        private var VARIANT_VIOLIN = PaletteEntry("chart-js.violin",
            "Violin",
            "A violin plot depicts distributions of numeric data for one or more groups using density curves. The width of each curve corresponds with the approximate frequency of data points in each region.",
            getIcon("/images/thumbnails/chart-js.violin.png"),
            getJsonProps("/variants/chart-js.violin.props.json")
        )

        var DESCRIPTOR_BUILDER = ComponentDescriptorImpl.ComponentBuilder.newBuilder()
            .setPaletteCategory("chart")
            .setId(COMPONENT_ID)
            .setModuleId(MODULE_ID)
            .setSchema(SCHEMA)
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
    }
}