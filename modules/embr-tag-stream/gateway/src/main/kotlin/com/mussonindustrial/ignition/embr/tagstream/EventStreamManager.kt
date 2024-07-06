package com.mussonindustrial.ignition.embr.tagstream

import com.inductiveautomation.ignition.common.auth.security.level.SecurityLevelConfig
import com.inductiveautomation.ignition.common.gson.*
import com.inductiveautomation.ignition.common.tags.model.SecurityContext
import com.mussonindustrial.ignition.embr.common.logging.getLogger
import com.mussonindustrial.ignition.embr.tagstream.emitters.EventEmitter
import com.mussonindustrial.ignition.embr.tagstream.emitters.RegisteredEventEmitter
import org.eclipse.jetty.io.EofException
import org.eclipse.jetty.servlets.EventSource
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class EventStreamManager(context: EventStreamGatewayContext) {

    private val logger = this.getLogger()
    private val executionManager = context.executionManager
    private val systemTags = context.systemTagsProvider
    private val emitterTypes = hashMapOf<String, RegisteredEventEmitter<out EventEmitter>>()
    private val sessions = hashMapOf<String, Session>()

    fun createSession(eventEmitters: List<EventEmitter>, securityContext: SecurityContext): Session {
        val session = Session(eventEmitters, securityContext)
        sessions[session.id] = session
        updateMetrics()
        return session
    }

    fun createSession(subscriptionProps: Map<String, JsonElement>, securityContext: SecurityContext): Session {
        val eventEmitters = subscriptionProps.map { (key, props) ->
            val constructor = emitterTypes[key]?.clazz?.getConstructor()
            if (constructor == null) {
                logger.warn("No event emitter found for key '$key'.")
                null
            } else {
                val emitter = constructor.newInstance()
                emitter.initialize(props)
                emitter
            }
        }.filterNotNull()
        return createSession(eventEmitters, securityContext)
    }

    fun getUnopenedSession(id: String): Session? {
        val session = sessions[id]
        if (session?.opened?.get() == true) return null
        updateMetrics()
        return session
    }

    fun getSession(id: String): Session? {
        val session = sessions[id]
        updateMetrics()
        return session
    }

    fun removeSession(session: Session) {
        sessions.remove(session.id)?.close()
        updateMetrics()
    }

    fun closeAllSessions() {
        logger.debug("Closing all sessions.")
        sessions.values.forEach { it.close() }
        sessions.clear()
        updateMetrics()
    }

    fun <T : EventEmitter> registerEmitter(key: String, emitter: Class<T>, gsonAdapter: JsonSerializer<T>) {
        emitterTypes[key] = RegisteredEventEmitter(emitter, gsonAdapter)
    }

    fun removeEmitter(key: String) {
        emitterTypes.remove(key)
    }

    private fun updateMetrics() {
        systemTags.sessionCountConnected = sessions.count { (_, session) -> session.opened.plain }
        systemTags.sessionCountUnconnected = sessions.count { (_, session) -> !session.opened.plain }
    }

    inner class Session(private val eventEmitters: List<EventEmitter>, val securityContext: SecurityContext): EventSource {
        private val logger = this.getLogger()
        private var emitter: EventSource.Emitter? = null
        val id = UUID.randomUUID().toString()
        val opened = AtomicBoolean(false)

        init {
            eventEmitters.forEach { it.onCreation(this) }
        }

        private val gsonAdapter = JsonSerializer<Session> { _, _, context ->
            JsonObject().apply {
                addProperty("session_id", id)
                add("security_context", context.serialize(securityContext))
                add("emitters", JsonObject().apply {
                    eventEmitters.forEach {
                        add(it.key, context.serialize(it))
                    }
                })
            }
        }
        private val gson: Gson = GsonBuilder().apply {
            registerTypeAdapter(Session::class.java, gsonAdapter)
            registerTypeAdapter(SecurityContext::class.java, SecurityContext.GsonAdapter())
            registerTypeAdapter(SecurityLevelConfig::class.java, SecurityLevelConfig.GsonAdapter(true))
            emitterTypes.values.forEach { registerTypeAdapter(it.clazz, it.gsonAdapter) }
        }.create()
        fun toGson(): JsonObject = this.gson.toJsonTree(this).asJsonObject

        private val doTimeout = executionManager.executeOnce({
            logger.warn("Session {} timed out before a connection was established.", id)
            removeSession(this) }, 30, TimeUnit.SECONDS)

        override fun onOpen(emitter: EventSource.Emitter) {
            logger.debug("Opening session {}", id)
            logger.trace("Connected Sessions: ${systemTags.sessionCountConnected}")
            doTimeout.cancel(true)
            opened.set(true)

            this.emitter = emitter
            emitter.event("session_open", toGson().toString())

            eventEmitters.forEach { it.onOpen() }
        }

        override fun onClose() {
            if (opened.getAndSet(false)) {
                logger.debug("Client initiated session close: {}", id)
                doTimeout.cancel(true)
                eventEmitters.forEach { it.onClose() }
                emitter = null
                removeSession(this)
            }
        }

        fun close() {
            if (opened.getAndSet(false)) {
                logger.debug("Server initiated session close: {}", id)
                doTimeout.cancel(true)
                eventEmitters.forEach { it.onClose() }
                emitter?.close()
                emitter = null
                removeSession(this)
            }
        }

        @Suppress("unused")
        fun emitData(data: String) = emitter?.data(data)

        @Suppress("unused")
        fun emitComment(comment: String) = emitter?.comment(comment)

        @Suppress("unused")
        fun emitEvent(event: String, data: String) = emitter?.event(event, data)

        fun closeOnException(block: () -> Unit) {
            try {
                block()
            } catch (e: EofException) {
                logger.debug("session ${this@Session.id}, connection dropped.", e)
                close()
            } catch (e: Throwable) {
                logger.warn("Error occurred during session ${this@Session.id}.", e)
                close()
            }
        }
    }
}



