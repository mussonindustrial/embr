package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.perspective.common.api.BrowserResource
import java.util.UUID

object PeriscopeComponents {

    private val instanceHash = UUID.randomUUID().toString().replace("-", "").substring(0, 16)

    private val JS_RESOURCE =
        BrowserResource(
            "embr-periscope-client-js",
            "/data/embr-periscope/resources/embr-periscope-client.js",
            BrowserResource.ResourceType.JS,
            instanceHash,
        )

    private val CSS_RESOURCE =
        BrowserResource(
            "embr-periscope-css",
            "/data/embr-periscope/resources/embr-periscope.css",
            BrowserResource.ResourceType.CSS,
            instanceHash,
        )
    val BROWSER_RESOURCES = mutableSetOf(JS_RESOURCE, CSS_RESOURCE)
    val REQUIRED_RESOURCES = mutableSetOf(JS_RESOURCE, CSS_RESOURCE)
}
