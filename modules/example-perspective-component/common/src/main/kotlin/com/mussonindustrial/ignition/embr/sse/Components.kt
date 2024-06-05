package com.mussonindustrial.ignition.embr.sse

import com.inductiveautomation.perspective.common.api.BrowserResource
import com.mussonindustrial.ignition.embr.sse.Meta.URL_ALIAS

object Components {
    const val COMPONENT_CATEGORY = "Embr"
    val BROWSER_RESOURCES: Set<BrowserResource> = mutableSetOf(
        BrowserResource(
            "example-component-js", "/res/${URL_ALIAS}/example-perspective-component-client.js",
            BrowserResource.ResourceType.JS
        )
    )
}