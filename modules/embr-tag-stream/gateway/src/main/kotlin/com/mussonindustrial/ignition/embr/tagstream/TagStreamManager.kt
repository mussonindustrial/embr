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
import com.mussonindustrial.ignition.embr.tagstream.alarming.SimpleAlarmListener
import org.eclipse.jetty.servlets.EventSource
import java.util.*
import java.util.concurrent.TimeUnit

class TagStreamManager(context: TagStreamGatewayContext) {

    private val executionManager = context.executionManager
    private val tagManager = context.tagManager
    private val alarmManager = context.alarmManager
    private val metricsProvider = context.tagStreamMetricsProvider
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

    fun removeSession(id: String) {
        unconnectedSessions.remove(id)
        connectedSessions.remove(id)
        updateMetrics()
    }

    fun closeAllStreams() {
        connectedSessions.values.forEach { it.close() }
        unconnectedSessions.clear()
        connectedSessions.clear()
        updateMetrics()
    }

    private fun updateMetrics() {
        metricsProvider.setUnconnectedSessionCount(unconnectedSessions.size)
        metricsProvider.setConnectedSessionCount(connectedSessions.size)
    }

    inner class Session(paths: List<String>): EventSource {
        private val logger = this.getLogger()
        val id = UUID.randomUUID().toString()

        private val tagPaths = paths.map { TagPathParser.parse(it) }
        private val tagIds = tagPaths.withIndex().associate { it.value to it.index}
        private val tagListeners = tagIds.map { TagListener(it.value, it.key) }
        private val tagGson = TagGson.create()

        private var emitter: EventSource.Emitter? = null

        private val doTimeout = executionManager.executeOnce({
            logger.warn("Session {} timed out before a connection was established.", id)
            removeSession(id) }, 30, TimeUnit.SECONDS)

        fun asGson(): JsonObject {
            val json = JsonObject()
            json.addProperty("session_id", id)

            val tags = JsonArray()
            this.tagListeners.forEach {
                val tag = JsonObject()
                tag.addProperty("tag_path", it.tagPath.toString())
                tag.addProperty("alarm_path", it.alarmPath.toString())
                tag.addProperty("tag_id", it.id)
                tags.add(tag)
            }
            json.add("tags", tags)
            return json
        }

        override fun onOpen(emitter: EventSource.Emitter) {
            logger.debug("New event source opened.")
            logger.trace("Connected Sessions: ${connectedSessions.size}")
            doTimeout.cancel(true)

            this.emitter = emitter
            emitter.event("session_open", asGson().toString())

            tagManager.subscribeAsync(tagPaths, tagListeners)
            tagListeners.forEach { alarmManager.addListener(it.alarmPath, it) }
        }

        override fun onClose() {
            logger.debug("Closing EventSource Session {}.", id)
            close()
        }

        fun close() {
            logger.debug("Closing Session {}.", id)
            doTimeout.cancel(true)
            tagManager.unsubscribeAsync(tagPaths, tagListeners)
            tagListeners.forEach { alarmManager.removeListener(it.alarmPath, it) }

            try {
                emitter?.close()
            } catch (e: Throwable) {
                logger.warn("Error closing Session ${id}.", e)
            }

            removeSession(this.id)
        }


        inner class TagListener(val id: Int, val tagPath: TagPath): TagChangeListener, SimpleAlarmListener {

            val alarmPath: QualifiedPath = QualifiedPath.Builder()
                .setProvider(tagPath.source)
                .setTag(tagPath.toStringPartial())
                .build()
            override fun tagChanged(event: TagChangeEvent) {
                logger.trace("alarmEvent: {}", event)

                try {
                    logger.trace("Notifying session of tag change event for {}.", tagPath)
                    val data = tagGson.toJsonTree(event.value)
                    (data as JsonObject).let {
                        it.addProperty("tag_id", id)
                        it.add("v", it.remove("value"))
                        it.add("t", it.remove("timestamp"))
                        it.add("q", it.remove("quality"))
                    }
                    emitter?.event("tag_change", data.toString())
                } catch (e: org.eclipse.jetty.io.EofException) {
                    logger.debug("Session $id was closed remotely.", e)
                    close()
                } catch (e: Throwable) {
                    logger.warn("Error during Session ${id}. Dropping bad session.", e)
                    close()
                }
            }

            override fun onAlarmEvent(event: AlarmEvent) {
                logger.trace("alarmEvent: {}", event)

                try {
                    logger.trace("Notifying session of alarm event for {}.", tagPath)
                    val data = JsonObject()
                    data.let {
                        it.addProperty("tag_id", id)
                        it.addProperty("count", event.count)
                        it.addProperty("displayPath", event.displayPath.toString())
                        it.addProperty("eventData", event.ackData ?: event.activeData ?: event.clearedData ?: EventData())
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
                } catch (e: org.eclipse.jetty.io.EofException) {
                    logger.debug("Session $id was closed remotely.", e)
                    close()
                } catch (e: Throwable) {
                    logger.warn("Error during Session ${id}. Dropping bad session.", e)
                    close()
                }
            }
        }
    }
}



