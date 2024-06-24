package com.mussonindustrial.ignition.embr.charts

import com.inductiveautomation.perspective.common.api.BrowserResource

object Components {
    val BROWSER_RESOURCES: Set<BrowserResource> = mutableSetOf(
        BrowserResource(
            "chart-js-component-js", "/res/${Meta.MODULE.shortId}/embr-chart-js-client.js",
            BrowserResource.ResourceType.JS
        )
    )
}