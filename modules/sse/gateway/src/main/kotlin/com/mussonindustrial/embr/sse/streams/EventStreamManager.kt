package com.mussonindustrial.embr.sse.streams

import com.inductiveautomation.ignition.common.auth.security.level.SecurityLevelConfig
import com.inductiveautomation.ignition.common.gson.Gson
import com.inductiveautomation.ignition.common.gson.GsonBuilder
import com.inductiveautomation.ignition.common.gson.JsonElement
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.gson.JsonSerializer
import com.inductiveautomation.ignition.common.tags.model.SecurityContext
import com.mussonindustrial.embr.common.gson.SimpleGsonAdapter
import com.mussonindustrial.embr.common.gson.SimpleJsonSerializable
import com.mussonindustrial.embr.common.logging.getLogger
import com.mussonindustrial.embr.sse.EventStreamGatewayContext
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Supplier
import org.eclipse.jetty.io.EofException
import org.eclipse.jetty.servlets.EventSource

class EventStreamManager(val context: EventStreamGatewayContext) {
    private val logger = this.getLogger()
    private val systemTags = context.systemTagsProvider
    private val streamTypes = hashMapOf<String, Supplier<EventStream>>()
    private val sessions = hashMapOf<String, Session>()

    fun createSession(
        eventStreams: List<EventStream>,
        securityContext: SecurityContext,
        sessionType: SessionType,
    ): Session {
        val session = Session(eventStreams, securityContext, sessionType)
        sessions[session.id] = session
        updateMetrics()
        return session
    }

    fun createSession(
        subscriptionProps: Map<String, JsonElement>,
        securityContext: SecurityContext,
        sessionType: SessionType,
    ): Session {
        val eventStreams = mutableListOf<EventStream>()
        subscriptionProps.forEach {
            val streamType = streamTypes[it.key]
            if (streamType == null) {
                logger.warn("No event emitter supplier found for key '${it.key}'.")
                return@forEach
            }
            val stream = streamType.get()
            logger.debug("initializing stream '{}': {}", it.key, it.value)
            stream.initialize(it.value)
            eventStreams.add(stream)
        }
        return createSession(eventStreams, securityContext, sessionType)
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

    fun registerStreamType(key: String, supplier: Supplier<EventStream>) {
        streamTypes[key] = supplier
    }

    fun removeStreamType(key: String) {
        streamTypes.remove(key)
    }

    private fun updateMetrics() {
        systemTags.sessionCountConnected = sessions.count { (_, session) -> session.opened.plain }
        systemTags.sessionCountUnconnected =
            sessions.count { (_, session) -> !session.opened.plain }
    }

    enum class SessionType {
        WEB,
        PERSPECTIVE,
    }

    inner class Session(
        private val eventStreams: List<EventStream>,
        val securityContext: SecurityContext,
        val sessionType: SessionType,
    ) : EventSource, SimpleJsonSerializable {
        private val logger = this.getLogger()
        private var emitter: EventSource.Emitter? = null
        val id = UUID.randomUUID().toString()
        val opened = AtomicBoolean(false)

        init {
            eventStreams.forEach { it.onCreate(this) }
        }

        private val gsonAdapter =
            JsonSerializer<Session> { _, _, context ->
                JsonObject().apply {
                    addProperty("session_id", id)
                    add("security_context", context.serialize(securityContext))
                    add(
                        "streams",
                        JsonObject().apply {
                            eventStreams.forEach { add(it.key, context.serialize(it)) }
                        },
                    )
                }
            }
        private val gson: Gson =
            GsonBuilder()
                .apply {
                    registerTypeAdapter(Session::class.java, gsonAdapter)
                    registerTypeAdapter(SecurityContext::class.java, SecurityContext.GsonAdapter())
                    registerTypeAdapter(
                        SecurityLevelConfig::class.java,
                        SecurityLevelConfig.GsonAdapter(true),
                    )
                    streamTypes.values.forEach {
                        val emitter = it.get()
                        registerTypeAdapter(emitter::class.java, SimpleGsonAdapter<EventStream>())
                    }
                }
                .create()

        override fun toGson(): JsonObject = this.gson.toJsonTree(this).asJsonObject

        private val doTimeout =
            context.eventStreamExecutionManager.executeOnce(
                {
                    logger.warn("Session {} timed out before a connection was established.", id)
                    removeSession(this)
                },
                30,
                TimeUnit.SECONDS,
            )

        override fun onOpen(emitter: EventSource.Emitter) {
            logger.debug("Opening session {}", id)
            logger.trace("Connected Sessions: ${systemTags.sessionCountConnected}")
            doTimeout.cancel(true)
            opened.set(true)

            this.emitter = emitter
            emitter.event("session_open", toGson().toString())

            eventStreams.forEach { it.onOpen() }
        }

        override fun onClose() {
            if (opened.getAndSet(false)) {
                logger.debug("Client initiated session close: {}", id)
                doTimeout.cancel(true)
                eventStreams.forEach { it.onClose() }
                emitter = null
                removeSession(this)
            }
        }

        fun close() {
            if (opened.getAndSet(false)) {
                logger.debug("Server initiated session close: {}", id)
                doTimeout.cancel(true)
                eventStreams.forEach { it.onClose() }
                emitter?.close()
                emitter = null
                removeSession(this)
            }
        }

        @Suppress("unused") fun emitData(data: String) = emitter?.data(data)

        @Suppress("unused") fun emitComment(comment: String) = emitter?.comment(comment)

        @Suppress("unused") fun emitEvent(event: String, data: String) = emitter?.event(event, data)

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
