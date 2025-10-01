package com.mussonindustrial.ignition.embr.muijoy

import com.inductiveautomation.perspective.common.api.BrowserResource
import com.mussonindustrial.ignition.embr.muijoy.Meta.SHORT_MODULE_ID

object MuiJoyComponents {

    private val JS_RESOURCE =
        BrowserResource(
            "embr-muijoy-client-js",
            "/res/${SHORT_MODULE_ID}/embr-muijoy-client.js",
            BrowserResource.ResourceType.JS,
        )

    private val CSS_RESOURCE =
        BrowserResource(
            "embr-muijoy-css",
            "/res/${SHORT_MODULE_ID}/embr-muijoy.css",
            BrowserResource.ResourceType.CSS,
        )
    val BROWSER_RESOURCES = mutableSetOf(JS_RESOURCE, CSS_RESOURCE)
    val REQUIRED_RESOURCES = mutableSetOf(JS_RESOURCE, CSS_RESOURCE)
}
