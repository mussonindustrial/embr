package com.mussonindustrial.ignition.embr.charts

import com.inductiveautomation.perspective.common.api.BrowserResource
import com.mussonindustrial.embr.common.Embr

object Components {
    val BROWSER_RESOURCES: Set<BrowserResource> =
        mutableSetOf(
            BrowserResource(
                "embr-charts-client",
                "/res/${Embr.CHARTS.shortId}/embr-charts-client.js",
                BrowserResource.ResourceType.JS
            )
        )
}
