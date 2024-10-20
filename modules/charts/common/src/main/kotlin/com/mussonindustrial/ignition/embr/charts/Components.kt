package com.mussonindustrial.ignition.embr.charts

import com.inductiveautomation.perspective.common.api.BrowserResource
import com.mussonindustrial.ignition.embr.charts.Meta.SHORT_MODULE_ID

object Components {
    val BROWSER_RESOURCES: Set<BrowserResource> =
        mutableSetOf(
            BrowserResource(
                "embr-charts-client",
                "/res/${SHORT_MODULE_ID}/embr-charts-client.js",
                BrowserResource.ResourceType.JS
            )
        )
}
