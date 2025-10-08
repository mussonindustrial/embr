package com.mussonindustrial.ignition.embr.charts.component.chart

import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.inductiveautomation.perspective.common.api.ComponentEventDescriptor
import com.mussonindustrial.embr.perspective.common.component.PaletteEntry
import com.mussonindustrial.embr.perspective.common.component.PerspectiveComponent
import com.mussonindustrial.embr.perspective.common.component.addPaletteEntry
import com.mussonindustrial.ignition.embr.charts.Components
import com.mussonindustrial.ignition.embr.charts.Meta.MODULE_ID

class ApexChartsLegacy {
    companion object : PerspectiveComponent {
        override val id: String = "kyvislabs.display.apexchart"

        private val VARIANT_BASE =
            PaletteEntry(
                this::class.java,
                id,
                "base",
                "ApexCharts (Legacy)",
                "A Kyvis-Labs compatible ApexCharts component.",
            )
        private val VARIANT_LINE =
            PaletteEntry(
                this::class.java,
                id,
                "line",
                "ApexChart Line",
                "An ApexChart line component.",
            )
        private val VARIANT_PIE =
            PaletteEntry(
                this::class.java,
                id,
                "pie",
                "ApexChart Pie",
                "An ApexChart pie component.",
            )
        private val VARIANT_RADAR =
            PaletteEntry(
                this::class.java,
                id,
                "radar",
                "ApexChart Radar",
                "An ApexChart radar component.",
            )
        private val VARIANT_TIMESERIES =
            PaletteEntry(
                this::class.java,
                id,
                "timeseries",
                "ApexChart Time Series",
                "An ApexChart time series component.",
            )

        fun eventDescriptor(
            name: String,
            description: String,
            schemaName: String,
        ): ComponentEventDescriptor {
            return ComponentEventDescriptor(
                name,
                description,
                JsonSchema.parse(
                    Components::class
                        .java
                        .getResourceAsStream(
                            "/schemas/components/${id}/events/${schemaName}.props.json"
                        )
                ),
            )
        }

        val ANIMATION_END_HANDLER =
            eventDescriptor(
                "animationEndHandler",
                "Fires when the chart’s initial animation is finished",
                "empty",
            )
        val BEFORE_MOUNT_HANDLER =
            eventDescriptor(
                "beforeMountHandler",
                "Fires before the chart has been drawn on screen",
                "empty",
            )
        val MOUNTED_HANDLER =
            eventDescriptor(
                "mountedHandler",
                "Fires after the chart has been drawn on screen",
                "empty",
            )
        val UPDATED_HANDLER =
            eventDescriptor(
                "updatedHandler",
                "Fires when the chart has been dynamically updated either with updateOptions() or updateSeries() functions",
                "empty",
            )
        val CLICK_HANDLER =
            eventDescriptor(
                "clickHandler",
                "Fires when user clicks on any area of the chart.",
                "mouse",
            )
        val MOUSE_MOVE_HANDLER =
            eventDescriptor(
                "mouseMoveHandler",
                "Fires when user moves mouse on any area of the chart.",
                "mouse",
            )
        val MOUSE_LEAVE_HANDLER =
            eventDescriptor(
                "mouseLeaveHandler",
                "Fires when user moves mouse outside chart area (exclusing axis).",
                "mouse",
            )
        val LEGEND_CLICK_HANDLER =
            eventDescriptor(
                "legendClickHandler",
                "Fires when user clicks on legend.",
                "seriesindex",
            )
        val MARKER_CLICK_HANDLER =
            eventDescriptor("markerClickHandler", "Fires when user clicks on the markers.", "mouse")
        val X_AXIS_LABEL_CLICK_HANDLER =
            eventDescriptor(
                "xAxisLabelClickHandler",
                "Fires when user clicks on the x-axis labels.",
                "mouse",
            )
        val SELECTION_HANDLER =
            eventDescriptor(
                "selectionHandler",
                "Fires when user selects rect using the selection tool.",
                "xaxis",
            )
        val DATA_POINT_SELECTION_HANDLER =
            eventDescriptor(
                "dataPointSelectionHandler",
                "Fires when user clicks on a datapoint (bar/column/marker/bubble/donut-slice).",
                "mouse",
            )
        val DATA_POINT_MOUSE_ENTER_HANDLER =
            eventDescriptor(
                "dataPointMouseEnterHandler",
                "Fires when user’s mouse enter on a datapoint (bar/column/marker/bubble/donut-slice).",
                "mouse",
            )
        val DATA_POINT_MOUSE_LEAVE_HANDLER =
            eventDescriptor(
                "dataPointMouseLeaveHandler",
                "MouseLeave event for a datapoint (bar/column/marker/bubble/donut-slice).",
                "mouse",
            )
        val ZOOMED_HANDLER =
            eventDescriptor(
                "zoomedHandler",
                "Fires when user zooms in/out the chart using either the selection zooming tool or zoom in/out buttons.",
                "xaxis",
            )
        val BEFORE_ZOOM_HANDLER =
            eventDescriptor(
                "beforeZoomHandler",
                "This function, if defined, runs just before zooming in/out of the chart allowing you to set a custom range for zooming in/out.",
                "xaxis",
            )
        val BEFORE_RESET_ZOOM_HANDLER =
            eventDescriptor(
                "beforeResetZoomHandler",
                "This function, if defined, runs just before resetting the zoomed chart to the original state.",
                "xaxis",
            )
        val SCROLLED_HANDLER =
            eventDescriptor(
                "scrolledHandler",
                "Fires when user scrolls using the pan tool.",
                "xaxis",
            )
        val BRUSH_SCROLLED_HANDLER =
            eventDescriptor(
                "brushScrolledHandler",
                "Fires when user drags the brush in a brush chart.",
                "xaxis",
            )

        override val descriptor: ComponentDescriptor =
            ComponentDescriptorImpl.ComponentBuilder.newBuilder()
                .setPaletteCategory("chart")
                .setId(id)
                .setModuleId(MODULE_ID)
                .setSchema(schema)
                .setEvents(
                    listOf(
                        ANIMATION_END_HANDLER,
                        BEFORE_MOUNT_HANDLER,
                        MOUNTED_HANDLER,
                        UPDATED_HANDLER,
                        CLICK_HANDLER,
                        MOUSE_MOVE_HANDLER,
                        MOUSE_LEAVE_HANDLER,
                        LEGEND_CLICK_HANDLER,
                        MARKER_CLICK_HANDLER,
                        X_AXIS_LABEL_CLICK_HANDLER,
                        SELECTION_HANDLER,
                        DATA_POINT_SELECTION_HANDLER,
                        DATA_POINT_MOUSE_ENTER_HANDLER,
                        DATA_POINT_MOUSE_LEAVE_HANDLER,
                        BEFORE_ZOOM_HANDLER,
                        ZOOMED_HANDLER,
                        BEFORE_RESET_ZOOM_HANDLER,
                        SCROLLED_HANDLER,
                        BRUSH_SCROLLED_HANDLER,
                    )
                )
                .setName("ApexCharts (Legacy)")
                .addPaletteEntry(VARIANT_BASE)
                .addPaletteEntry(VARIANT_LINE)
                .addPaletteEntry(VARIANT_PIE)
                .addPaletteEntry(VARIANT_RADAR)
                .addPaletteEntry(VARIANT_TIMESERIES)
                .setDefaultMetaName("ApexChartsLegacy")
                .setResources(Components.BROWSER_RESOURCES)
                .build()
    }
}
