package com.mussonindustrial.embr.common

import com.inductiveautomation.ignition.common.model.PlatformEdition

object Embr {
    val CHARTS = EmbrModuleMeta("com.mussonindustrial.embr.charts", "embr-charts", "/embr/charts")
    val PERISCOPE =
        EmbrModuleMeta("com.mussonindustrial.embr.periscope", "embr-periscope", "/embr/periscope")
    val EVENT_STREAM =
        EmbrModuleMeta(
            "com.mussonindustrial.embr.eventstream",
            "embr-event-stream",
            "/embr/event-stream",
        )
    val SNMP = EmbrModuleMeta("com.mussonindustrial.embr.snmp", "embr-snmp", "/embr/snmp")
    val THERMO = EmbrModuleMeta("com.mussonindustrial.embr.thermo", "embr-thermo", "/embr/thermo")

    const val DOCUMENTATION_URL = "https://docs.mussonindustrial.com/"

    fun isLicenseRequired(): Boolean {
        return !PlatformEdition.isMaker()
    }
}
