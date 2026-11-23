package com.mussonindustrial.ignition.embr.periscope.component

import com.google.common.eventbus.Subscribe
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.perspective.common.api.PropertyType
import com.inductiveautomation.perspective.gateway.api.Component
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.inductiveautomation.perspective.gateway.event.SessionShutdownEvent
import com.inductiveautomation.perspective.gateway.event.SessionStartupEvent
import com.inductiveautomation.perspective.gateway.model.MessageChannel
import com.inductiveautomation.perspective.gateway.property.PropertyTree
import com.inductiveautomation.perspective.gateway.session.InternalSession
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.perspective.gateway.reflect.getHandlers
import com.mussonindustrial.embr.perspective.gateway.reflect.unregister
import com.mussonindustrial.ignition.embr.periscope.resources.ComponentAttributes
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ProjectComponentDefaultsManager(private val perspectiveContext: PerspectiveContext) {

    companion object {
        const val PROTOCOL = "periscope-component-defaults"
    }

    private val logger = this.getLoggerEx()
    private val definitionsByPage = ConcurrentHashMap<PageKey, Map<String, Definition>>()
    private val listeningSessions = ConcurrentHashMap.newKeySet<UUID>()

    fun startup() {
        perspectiveContext.eventBus.register(this)
        perspectiveContext.sessionMonitor.getRunningSessions().forEach { addHandler(it) }
    }

    fun shutdown() {
        perspectiveContext.eventBus.unregister(this)
        perspectiveContext.sessionMonitor.getRunningSessions().forEach { session ->
            logger.debug("Removing component defaults handler from session '${session.sessionId}'.")
            session.getHandlers()?.unregister(PROTOCOL)
        }
        definitionsByPage.clear()
        listeningSessions.clear()
    }

    fun addHandler(session: InternalSession) {
        val handlers = session.getHandlers() ?: return
        if (!listeningSessions.add(session.sessionId)) {
            return
        }

        if (!handlers.handles(PROTOCOL)) {
            logger.debug("Adding component defaults handler to session '${session.sessionId}'.")
            handlers.register(
                PROTOCOL,
                { channel: MessageChannel, components: JsonObject ->
                    storeDefinitions(session, channel.pageId, components)
                },
                JsonObject::class.java,
            )
        }
    }

    fun fillProps(component: Component) {
        val defaults = definitionOf(component)?.defaults ?: return
        component.getPropertyTreeOf(PropertyType.props)?.let { fillMissing(it, defaults) }
    }

    fun fillPositions(component: Component) {
        val defaults = definitionOf(component)?.positionDefaults ?: return
        component.children.forEach { child ->
            child.getPropertyTreeOf(PropertyType.position)?.let { fillMissing(it, defaults) }
        }
    }

    @Subscribe
    fun onSessionStarted(event: SessionStartupEvent) {
        addHandler(event.session)
    }

    @Subscribe
    fun onSessionShutdown(event: SessionShutdownEvent) {
        val sessionId = event.session.sessionId
        listeningSessions.remove(sessionId)
        definitionsByPage.keys.removeIf { it.sessionId == sessionId }
    }

    private fun storeDefinitions(session: InternalSession, pageId: String, components: JsonObject) {
        removeClosedPages()

        val definitions =
            components
                .entrySet()
                .filter { (componentId, value) ->
                    ComponentAttributes.isValidId(componentId) && value.isJsonObject
                }
                .associate { (componentId, value) ->
                    val json = value.asJsonObject
                    val defaults = json.get("defaults") as? JsonObject ?: JsonObject()
                    val positionDefaults = json.get("positionDefaults") as? JsonObject
                    componentId to Definition(defaults, positionDefaults)
                }

        definitionsByPage[PageKey(session.sessionId, pageId)] = definitions
    }

    private fun removeClosedPages() {
        val sessionMonitor = perspectiveContext.sessionMonitor
        definitionsByPage.keys.removeIf { key ->
            val page = sessionMonitor.findSession(key.sessionId).flatMap { it.findPage(key.pageId) }
            page.isEmpty
        }
    }

    private fun definitionOf(component: Component): Definition? {
        val key = PageKey(component.session.sessionId, component.page.id)
        return definitionsByPage[key]?.get(ProjectComponents.getId(component.type))
    }

    private fun fillMissing(tree: PropertyTree, defaults: JsonObject) {
        val missing = missingFrom(tree, defaults, "")
        if (missing.size() > 0) {
            tree.merge(missing)
        }
    }

    private fun missingFrom(tree: PropertyTree, defaults: JsonObject, prefix: String): JsonObject {
        val missing = JsonObject()

        defaults.entrySet().forEach { (key, value) ->
            val path = prefix + key
            if (!tree.read(path).isPresent) {
                missing.add(key, value)
            } else if (value.isJsonObject) {
                val missingChildren = missingFrom(tree, value.asJsonObject, "$path.")
                if (missingChildren.size() > 0) {
                    missing.add(key, missingChildren)
                }
            }
        }

        return missing
    }

    private class Definition(val defaults: JsonObject, val positionDefaults: JsonObject?)

    private data class PageKey(val sessionId: UUID, val pageId: String)
}
