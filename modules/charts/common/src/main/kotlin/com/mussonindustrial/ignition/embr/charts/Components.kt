package com.mussonindustrial.ignition.embr.charts

import com.inductiveautomation.perspective.common.api.BrowserResource
import com.mussonindustrial.ignition.embr.charts.Meta.SHORT_MODULE_ID

object Components {
    val BROWSER_RESOURCES: Set<BrowserResource> =
        mutableSetOf(
            BrowserResource(
                "chart-js-component-js",
                "/res/${SHORT_MODULE_ID}/embr-chart-js-client.js",
                BrowserResource.ResourceType.JS
            ),
            BrowserResource(
                "embr-charts-css",
                "/res/${SHORT_MODULE_ID}/style.css",
                BrowserResource.ResourceType.CSS
            )
        )
}
