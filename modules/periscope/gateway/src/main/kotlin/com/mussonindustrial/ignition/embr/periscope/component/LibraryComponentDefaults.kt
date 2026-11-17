package com.mussonindustrial.ignition.embr.periscope.component

import com.google.common.eventbus.Subscribe
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.perspective.common.api.PropertyType
import com.inductiveautomation.perspective.gateway.api.Component
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegate
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegateFactory
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.inductiveautomation.perspective.gateway.event.SessionStartupEvent
import com.inductiveautomation.perspective.gateway.model.MessageChannel
import com.inductiveautomation.perspective.gateway.property.PropertyTree
import com.inductiveautomation.perspective.gateway.session.InternalSession
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.perspective.gateway.reflect.getHandlers
import com.mussonindustrial.embr.perspective.gateway.reflect.unregister
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class LibraryComponentDefaults(private val perspective: PerspectiveContext) :
    ComponentModelDelegateFactory {

    private class Definition(val defaults: JsonObject, val positionDefaults: JsonObject?)

    private data class PageKey(val sessionId: UUID, val pageId: String)

    private val logger = this.getLoggerEx()
    private val definitionsByPage = ConcurrentHashMap<PageKey, Map<String, Definition>>()

    fun startup() {
        perspective.eventBus.register(this)
        perspective.sessionMonitor.runningSessions.forEach { listen(it) }
    }

    fun shutdown() {
        perspective.eventBus.unregister(this)
        perspective.sessionMonitor.runningSessions.forEach { session ->
            logger.debug("Removing component defaults handler from session ${session.sessionId}")
            session.getHandlers()?.unregister(PROTOCOL)
        }
        definitionsByPage.clear()
    }

    @Subscribe
    fun onSessionStarted(event: SessionStartupEvent) {
        listen(event.session)
    }

    @Synchronized
    fun listen(session: InternalSession) {
        val handlers = session.getHandlers() ?: return
        if (handlers.handles(PROTOCOL)) {
            return
        }

        logger.debug("Adding component defaults handler to session ${session.sessionId}")
        handlers.register(
            PROTOCOL,
            { channel: MessageChannel, components: JsonObject ->
                storeDefinitions(session, channel.pageId, components)
            },
            JsonObject::class.java,
        )
    }

    private fun storeDefinitions(session: InternalSession, pageId: String, components: JsonObject) {
        removeClosedPages()

        val definitions = mutableMapOf<String, Definition>()
        for ((componentId, value) in components.entrySet()) {
            if (COMPONENT_ID.matches(componentId) && value.isJsonObject) {
                definitions[componentId] = toDefinition(value.asJsonObject)
            }
        }

        definitionsByPage[PageKey(session.sessionId, pageId)] = definitions
    }

    private fun removeClosedPages() {
        val sessionMonitor = perspective.sessionMonitor
        definitionsByPage.keys.removeIf { key ->
            val page = sessionMonitor.findSession(key.sessionId).flatMap { it.findPage(key.pageId) }
            page.isEmpty
        }
    }

    private fun toDefinition(json: JsonObject): Definition {
        val defaults = json.getAsJsonObject("defaults") ?: JsonObject()
        val positionDefaults = json.getAsJsonObject("positionDefaults")
        return Definition(defaults, positionDefaults)
    }

    override fun create(component: Component): ComponentModelDelegate {
        applyDefaults(component)
        return noModelDelegate()
    }

    private fun applyDefaults(component: Component) {
        val key = PageKey(component.session.sessionId, component.page.id)
        val componentId = LibraryComponents.idOf(component.type)
        val definition = definitionsByPage[key]?.get(componentId) ?: return

        component.getPropertyTreeOf(PropertyType.props)?.let {
            fillMissing(it, definition.defaults)
        }

        val positionDefaults = definition.positionDefaults ?: return
        component.children.forEach { child ->
            child.getPropertyTreeOf(PropertyType.position)?.let {
                fillMissing(it, positionDefaults)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> noModelDelegate(): T {
        return null as T
    }

    private fun fillMissing(tree: PropertyTree, defaults: JsonObject) {
        val missing = findMissing(tree, defaults, "") ?: return
        tree.merge(missing)
    }

    private fun findMissing(tree: PropertyTree, defaults: JsonObject, prefix: String): JsonObject? {
        val missing = JsonObject()

        for ((key, value) in defaults.entrySet()) {
            val path = prefix + key
            if (!tree.read(path).isPresent) {
                missing.add(key, value)
            } else if (value.isJsonObject) {
                val missingChildren = findMissing(tree, value.asJsonObject, "$path.")
                if (missingChildren != null) {
                    missing.add(key, missingChildren)
                }
            }
        }

        if (missing.size() == 0) {
            return null
        }
        return missing
    }

    companion object {
        const val PROTOCOL = "embr-periscope-component-defaults"
        private val COMPONENT_ID = Regex("""^[A-Za-z][\w-]*\.[\w.-]{1,200}$""")
    }
}
