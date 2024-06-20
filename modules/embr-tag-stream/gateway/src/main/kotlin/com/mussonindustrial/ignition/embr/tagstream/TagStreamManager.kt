package com.mussonindustrial.ignition.embr.tagstream

import com.inductiveautomation.ignition.common.QualifiedPath
import com.inductiveautomation.ignition.common.alarming.AlarmEvent
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.tags.config.TagGson
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

    inner class Session(paths: List<String>): TagChangeListener, EventSource, SimpleAlarmListener {
        private val logger = this.getLogger()
        val id = UUID.randomUUID().toString()

        private val tagPaths = paths.map { TagPathParser.parse(it) }
        private val alarmPaths = tagPaths.map { QualifiedPath.Builder().setProvider(it.source).setTag(it.toStringPartial()).build() }
        private val tagIds = tagPaths.withIndex().associate { it.value to it.index}
        private val listeners = tagPaths.map { this }
        private val tagGson = TagGson.create()

        private var emitter: EventSource.Emitter? = null

        private val doTimeout = executionManager.executeOnce({
            logger.warn("Session {} timed out before a connection was established.", id)
            removeSession(id) }, 30, TimeUnit.SECONDS)

        fun getTagsJson(): JsonObject {
            val json = JsonObject()
            this.tagIds.forEach { (tagPath, id) ->
                val tag = JsonObject()
                tag.addProperty("path", tagPath.toString())
                tag.addProperty("id", id)
                json.add(tagPath.toString(), tag)
            }
            return json
        }

        override fun onOpen(emitter: EventSource.Emitter) {
            logger.debug("New event source opened.")
            logger.trace("Connected Sessions: ${connectedSessions.size}")
            doTimeout.cancel(true)

            this.emitter = emitter
            emitter.event("session_id", id)
            emitter.event("tags", getTagsJson().toString())

            tagManager.subscribeAsync(tagPaths, listeners)
            logger.info(alarmPaths.toString())
            alarmPaths.forEach { alarmManager.addListener(it, this) }
        }

        override fun onClose() {
            logger.debug("Closing EventSource Session {}.", id)
            close()
        }

        fun close() {
            logger.debug("Closing Session {}.", id)
            doTimeout.cancel(true)
            tagManager.unsubscribeAsync(tagPaths, listeners)
            alarmPaths.forEach { alarmManager.removeListener(it, this) }

            try {
                emitter?.close()
            } catch (e: Throwable) {
                logger.warn("Error closing Session ${id}.", e)
            }

            removeSession(this.id)
        }

        private fun notifyTagChange(event: TagChangeEvent) {
            val tagPath = event.tagPath.toString()
            logger.trace("Notifying session of tag change event for {}.", tagPath)
            val id = tagIds[event.tagPath].toString()

            val data = tagGson.toJsonTree(event.value)
            (data as JsonObject).let {
                it.add("v", it.remove("value"))
                it.add("t", it.remove("timestamp"))
                it.add("q", it.remove("quality"))
            }
            emitter?.event("id=$id", data.toString())
        }

        override fun tagChanged(event: TagChangeEvent) {
            logger.trace("tagChanged: {}", event)

            try {
                this.notifyTagChange(event)
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
            emitter?.event("id=$id:type=alarmEvent", event.toString())
        }
    }
}



