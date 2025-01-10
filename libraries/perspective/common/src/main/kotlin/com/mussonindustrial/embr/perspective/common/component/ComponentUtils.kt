package com.mussonindustrial.embr.perspective.common.component

import com.inductiveautomation.perspective.common.api.BrowserResource
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl
import com.inductiveautomation.perspective.common.api.ComponentRegistry

fun ComponentDescriptor.addBrowserResource(browserResource: BrowserResource) {
    val browserResources = this.browserResources()
    val newBrowserResources = browserResources.toMutableSet()
    newBrowserResources.add(browserResource)

    val field = ComponentDescriptorImpl::class.java.getDeclaredField("browserResources")
    field.setAccessible(true)
    field.set(this, newBrowserResources)
}

fun ComponentDescriptor.removeBrowserResource(browserResource: BrowserResource) {
    val browserResources = this.browserResources()
    val newBrowserResources = browserResources.toMutableSet()
    newBrowserResources.remove(browserResource)

    val field = ComponentDescriptorImpl::class.java.getDeclaredField("browserResources")
    field.setAccessible(true)
    field.set(this, newBrowserResources)
}

fun ComponentRegistry.addResourcesTo(
    resources: Set<BrowserResource>,
    predicate: (ComponentDescriptor) -> Boolean
) {
    this.get().values.forEach { component ->
        if (predicate(component)) {
            resources.forEach { component.addBrowserResource(it) }
        }
    }
}

fun ComponentRegistry.removeResourcesFrom(
    resources: Set<BrowserResource>,
    predicate: (ComponentDescriptor) -> Boolean
) {
    this.get().values.forEach { component ->
        if (predicate(component)) {
            resources.forEach { component.removeBrowserResource(it) }
        }
    }
}
