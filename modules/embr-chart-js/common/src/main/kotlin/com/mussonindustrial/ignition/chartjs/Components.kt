package com.mussonindustrial.ignition.chartjs

import com.inductiveautomation.perspective.common.api.BrowserResource
import com.mussonindustrial.ignition.chartjs.Meta.SHORT_MODULE_ID

object Components {
    const val COMPONENT_CATEGORY = "Embr"
    val BROWSER_RESOURCES: Set<BrowserResource> = mutableSetOf(
        BrowserResource(
            "chart-js-component-js", "/res/${SHORT_MODULE_ID}/embr-chart-js-client.js",
            BrowserResource.ResourceType.JS
        )
    )
}