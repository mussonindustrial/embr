package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.perspective.common.api.BrowserResource
import com.mussonindustrial.ignition.embr.periscope.Meta.SHORT_MODULE_ID

object Components {
    val BROWSER_RESOURCES: Set<BrowserResource> =
        mutableSetOf(
            // HACK: Need to force our resources to load AFTER
            // PerspectiveComponents.js. This seems to be alphabetical, so we add a prefix.
            BrowserResource(
                "zz-embr-periscope-client-js",
                "/res/${SHORT_MODULE_ID}/embr-periscope-client.js",
                BrowserResource.ResourceType.JS
            ),
            BrowserResource(
                "embr-periscope-css",
                "/res/${SHORT_MODULE_ID}/style.css",
                BrowserResource.ResourceType.CSS
            )
        )
}
