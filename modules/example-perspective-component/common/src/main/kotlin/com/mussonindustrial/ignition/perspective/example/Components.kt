package com.mussonindustrial.ignition.perspective.example

import com.inductiveautomation.perspective.common.api.BrowserResource
import com.mussonindustrial.ignition.perspective.example.Meta.URL_ALIAS

object Components {
    const val COMPONENT_CATEGORY = "Embr"
    val BROWSER_RESOURCES: Set<BrowserResource> = mutableSetOf(
        BrowserResource(
            "example-component-js", "/res/${URL_ALIAS}/example-perspective-component-client.js",
            BrowserResource.ResourceType.JS
        )
    )
}