package com.mussonindustrial.ignition.embr.periscope.component

import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.jvm.optionals.getOrNull

/**
 * Compiles outside of any lock of its own: compiling reads the project's resources, and a project
 * change holds those locked while it clears the cache and looks scripts up again.
 */
class ProjectComponentDelegateScriptCache(
    private val compile: (componentId: String) -> ProjectComponentDelegateScript?,
) {

    private val scriptsByComponent =
        AtomicReference(ConcurrentHashMap<String, Optional<ProjectComponentDelegateScript>>())

    fun find(componentId: String): ProjectComponentDelegateScript? {
        val scripts = scriptsByComponent.get()
        val cached = scripts[componentId]
        if (cached != null) {
            return cached.getOrNull()
        }

        val compiled = Optional.ofNullable(compile(componentId))
        return (scripts.putIfAbsent(componentId, compiled) ?: compiled).getOrNull()
    }

    fun clear() {
        scriptsByComponent.set(ConcurrentHashMap())
    }
}
