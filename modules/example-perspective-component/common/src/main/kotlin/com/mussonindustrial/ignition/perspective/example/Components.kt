package com.mussonindustrial.ignition.perspective.example

import com.inductiveautomation.perspective.common.api.BrowserResource


@Suppress("unused")
class Components {
    companion object {
        const val MODULE_ID = "com.mussonindustrial.perspective.example"
        const val URL_ALIAS = "mussonindustrial"
        const val COMPONENT_CATEGORY = "Musson Industrial"
        val BROWSER_RESOURCES: Set<BrowserResource> = java.util.Set.of(
            BrowserResource(
                "simple-component-js", "/res/${URL_ALIAS}/example-perspective-component.umd.js",
                BrowserResource.ResourceType.JS
            )
        )
    }
}