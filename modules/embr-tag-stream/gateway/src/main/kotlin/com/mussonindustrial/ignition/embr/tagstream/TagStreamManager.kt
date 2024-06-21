package com.mussonindustrial.ignition.embr.tagstream

import com.inductiveautomation.ignition.common.QualifiedPath
import com.inductiveautomation.ignition.common.alarming.AlarmEvent
import com.inductiveautomation.ignition.common.alarming.EventData
import com.inductiveautomation.ignition.common.gson.JsonArray
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.tags.config.TagGson
import com.inductiveautomation.ignition.common.tags.model.TagPath
import com.inductiveautomation.ignition.common.tags.model.event.TagChangeEvent
import com.inductiveautomation.ignition.common.tags.model.event.TagChangeListener
import com.inductiveautomation.ignition.common.tags.paths.parser.TagPathParser
import com.mussonindustrial.ignition.embr.common.gson.addProperty
import com.mussonindustrial.ignition.embr.common.logging.getLogger
import com.mussonindustrial.ignition.embr.tagstream.alarming.SimpleAlarmListener
import org.eclipse.jetty.io.EofException
import org.eclipse.jetty.servlets.EventSource
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class TagStreamManager(context: TagStreamGatewayContext) {

    private val logger = this.getLogger()
    private val executionManager = context.executionManager
    private val tagManager = context.tagManager
    private val alarmManager = context.alarmManager
    private val systemTags = context.tagStreamSystemTagsProvider
    private val unconnectedSessions = hashMapOf<String, Session>()
    private val connectedSessions = hashMapOf<String, Session>()

    fun createSession(paths: List<String>): Session {
        val session = Session(paths)
        unconnectedSessions[session.id] = session
        updateMetrics()
        return session
    }

    fun joinSession(id: String): Session? {
        val stream = unconnectedSessions.remove(id)
        stream?.let {
            connectedSessions[id] = stream
        }
        updateMetrics()
        return stream
    }

    fun removeSession(session: Session) {
        unconnectedSessions.remove(session.id)
        connectedSessions.remove(session.id)?.close()
        updateMetrics()
    }

    fun closeAllSessions() {
        logger.debug("Closing all sessions.")
        unconnectedSessions.clear()
        connectedSessions.values.forEach { it.close() }
        connectedSessions.clear()
        updateMetrics()
    }

    private fun updateMetrics() {
        systemTags.sessionCountConnected = connectedSessions.size
        systemTags.sessionCountUnconnected = unconnectedSessions.size
    }

    inner class Session(paths: List<String>): EventSource {
        private val logger = this.getLogger()
        val id = UUID.randomUUID().toString()
        private var opened = AtomicBoolean(false)

        private val tagPaths = paths.map { TagPathParser.parse(it) }
        private val tagIds = tagPaths.withIndex().associate { it.value to it.index}
        private val tagListeners = tagIds.map { TagListener(it.value, it.key) }
        private val tagGson = TagGson.create()

        private var emitter: EventSource.Emitter? = null

        private val doTimeout = executionManager.executeOnce({
            logger.warn("Session {} timed out before a connection was established.", id)
            removeSession(this) }, 30, TimeUnit.SECONDS)

        val sessionInfo = JsonObject().let { json ->
            json.addProperty("session_id", id)

            val tags = JsonArray()
            this.tagListeners.forEach { listener ->
                val tag = JsonObject()
                tag.addProperty("tag_path", listener.tagPath.toString())
                tag.addProperty("alarm_path", listener.alarmPath.toString())
                tag.addProperty("tag_id", id)
                tags.add(tag)
            }
            json.add("tags", tags)
            return@let json
        }

        override fun onOpen(emitter: EventSource.Emitter) {
            logger.debug("Opening session {}", id)
            logger.trace("Connected Sessions: ${connectedSessions.size}")
            doTimeout.cancel(true)
            opened.set(true)

            this.emitter = emitter
            emitter.event("session_open", sessionInfo.toString())

            tagListeners.forEach { it.subscribe() }
        }

        override fun onClose() {
            if (opened.getAndSet(false)) {
                logger.debug("Client initiated session close: {}", id)
                doTimeout.cancel(true)
                emitter = null
                tagListeners.forEach { it.unsubscribe() }
                removeSession(this)
            }
        }

        fun close() {
            if (opened.getAndSet(false)) {
                logger.debug("Server initiated session close: {}", id)
                doTimeout.cancel(true)
                emitter?.close()
                emitter = null
                tagListeners.forEach { it.unsubscribe() }
                removeSession(this)
            }
        }

        inner class TagListener(val id: Int, val tagPath: TagPath): TagChangeListener, SimpleAlarmListener {
            val alarmPath: QualifiedPath = QualifiedPath.Builder()
                .setProvider(tagPath.source)
                .setTag(tagPath.toStringPartial())
                .build()
            private var subscribed = AtomicBoolean(false)

            private fun closeOnException(runnable: () -> Unit) {
                try {
                    runnable()
                } catch (e: EofException) {
                    logger.debug("session ${this@Session.id}, connection dropped.", e)
                    close()
                } catch (e: Throwable) {
                    logger.warn("Error occurred during session ${this@Session.id}.", e)
                    close()
                }
            }

            fun subscribe() {
                logger.trace("session: {}, subscribing to {} events.", this@Session.id, tagPath)
                subscribed.set(true)
                tagManager.subscribeAsync(tagPath, this)
                alarmManager.addListener(alarmPath, this)
            }

            fun unsubscribe() {
                if (subscribed.getAndSet(false)) {
                    logger.trace("session: {}, unsubscribing from: {}", this@Session.id, tagPath)
                    tagManager.unsubscribeAsync(tagPath, this)
                    alarmManager.removeListener(alarmPath, this)
                }
            }

            override fun tagChanged(event: TagChangeEvent) {
                logger.trace("session: {}, tagChanged: {}", this@Session.id, event)
                closeOnException {
                    val data = tagGson.toJsonTree(event.value)
                    (data as JsonObject).let {
                        it.addProperty("tag_id", id)
                        it.add("v", it.remove("value"))
                        it.add("t", it.remove("timestamp"))
                        it.add("q", it.remove("quality"))
                    }
                    emitter?.event("tag_change", data.toString())
                }
            }

            override fun onAlarmEvent(event: AlarmEvent) {
                logger.trace("session: {}, alarmEvent: {}", this@Session.id, event)

                closeOnException {
                    val data = JsonObject()
                    data.let {
                        it.addProperty("tag_id", id)
                        it.addProperty("count", event.count)
                        it.addProperty("displayPath", event.displayPath.toString())
                        it.addProperty(
                            "eventData",
                            event.ackData ?: event.activeData ?: event.clearedData ?: EventData()
                        )
                        it.addProperty("extension", event.extension)
                        it.addProperty("id", event.id.toString())
                        it.addProperty("isAcked", event.isAcked)
                        it.addProperty("isCleared", event.isCleared)
                        it.addProperty("isShelved", event.isShelved)
                        it.addProperty("label", event.label)
                        it.addProperty("name", event.name)
                        it.addProperty("notes", event.notes)
                        it.addProperty("priority", event.priority.toString())
                        it.addProperty("source", event.source.toString())
                        it.addProperty("state", event.state.toString())
                        it.addProperty("values", event.values)
                    }
                    emitter?.event("alarm_event", data.toString())
                }
            }
        }
    }
}



