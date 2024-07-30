package com.mussonindustrial.ignition.embr.sse

import com.inductiveautomation.perspective.common.api.BrowserResource
import com.mussonindustrial.ignition.embr.sse.Meta.URL_ALIAS

object Components {
    val componentCategory = "Embr"
    val browserResources: Set<BrowserResource> =
        mutableSetOf(
            BrowserResource(
                "example-component-js",
                "/res/${URL_ALIAS}/example-perspective-component-client.js",
                BrowserResource.ResourceType.JS,
            ),
        )
}
