package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.perspective.common.api.BrowserResource
import com.mussonindustrial.ignition.embr.periscope.Meta.SHORT_MODULE_ID

object PeriscopeComponents {

    private val JS_RESOURCE =
        BrowserResource(
            "embr-periscope-client-js",
            "/res/${SHORT_MODULE_ID}/embr-periscope-client.js",
            BrowserResource.ResourceType.JS,
        )

    private val CSS_RESOURCE =
        BrowserResource(
            "embr-periscope-css",
            "/res/${SHORT_MODULE_ID}/style.css",
            BrowserResource.ResourceType.CSS,
        )
    val BROWSER_RESOURCES = mutableSetOf(JS_RESOURCE, CSS_RESOURCE)
    val REQUIRED_RESOURCES = mutableSetOf(JS_RESOURCE)
}
