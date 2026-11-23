package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.perspective.common.api.BrowserResource

object PeriscopeComponents {

    private val JS_RESOURCE =
        BrowserResource(
            "embr-periscope-client-js",
            PeriscopeRoutes.asset("embr-periscope-client.js"),
            BrowserResource.ResourceType.JS,
        )

    private val CSS_RESOURCE =
        BrowserResource(
            "embr-periscope-css",
            PeriscopeRoutes.asset("embr-periscope.css"),
            BrowserResource.ResourceType.CSS,
        )
    val BROWSER_RESOURCES = mutableSetOf(JS_RESOURCE, CSS_RESOURCE)
    val REQUIRED_RESOURCES = mutableSetOf(JS_RESOURCE, CSS_RESOURCE)
}
