package com.mussonindustrial.embr.common

object Embr {
    val CHARTS = EmbrModuleMeta("com.mussonindustrial.embr.charts", "embr-charts", "/embr/charts")

    val EVENT_STREAM =
        EmbrModuleMeta(
            "com.mussonindustrial.embr.eventstream",
            "embr-event-stream",
            "/embr/event-stream",
        )

    val PERISCOPE =
        EmbrModuleMeta("com.mussonindustrial.embr.periscope", "embr-periscope", "/embr/periscope")

    val THERMO = EmbrModuleMeta("com.mussonindustrial.embr.thermo", "embr-thermo", "/embr/thermo")
}
