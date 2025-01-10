package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.perspective.common.api.BrowserResource
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentRegistry
import com.mussonindustrial.embr.perspective.common.component.addBrowserResource
import com.mussonindustrial.embr.perspective.common.component.removeBrowserResource
import com.mussonindustrial.ignition.embr.periscope.Meta.SHORT_MODULE_ID

object PeriscopeComponents {

    private val JS_RESOURCE =
        BrowserResource(
            "embr-periscope-client-js",
            "/res/${SHORT_MODULE_ID}/embr-periscope-client.js",
            BrowserResource.ResourceType.JS
        )

    private val CSS_RESOURCE =
        BrowserResource(
            "embr-periscope-css",
            "/res/${SHORT_MODULE_ID}/style.css",
            BrowserResource.ResourceType.CSS
        )
    val BROWSER_RESOURCES: Set<BrowserResource> = mutableSetOf(JS_RESOURCE, CSS_RESOURCE)

    fun addResources(
        componentRegistry: ComponentRegistry,
        predicate: (ComponentDescriptor) -> Boolean
    ) {
        componentRegistry.get().values.forEach { component ->
            if (predicate(component)) {
                component.addBrowserResource(JS_RESOURCE)
            }
        }
    }

    fun removeResources(
        componentRegistry: ComponentRegistry,
        predicate: (ComponentDescriptor) -> Boolean
    ) {
        componentRegistry.get().values.forEach { component ->
            if (predicate(component)) {
                component.removeBrowserResource(JS_RESOURCE)
            }
        }
    }
}
