package com.mussonindustrial.ignition.embr.tagstream

import com.inductiveautomation.ignition.common.QualifiedPath
import com.inductiveautomation.ignition.common.StreamingDataset
import com.inductiveautomation.ignition.common.alarming.AlarmEvent
import com.inductiveautomation.ignition.common.alarming.EventData
import com.inductiveautomation.ignition.common.auth.security.level.SecurityLevelConfig
import com.inductiveautomation.ignition.common.gson.*
import com.inductiveautomation.ignition.common.sqltags.history.TagHistoryQueryParams
import com.inductiveautomation.ignition.common.sqltags.history.cache.TagHistoryCache
import com.inductiveautomation.ignition.common.tags.config.TagGson
import com.inductiveautomation.ignition.common.tags.model.SecurityContext
import com.inductiveautomation.ignition.common.tags.model.TagPath
import com.inductiveautomation.ignition.common.tags.model.event.TagChangeEvent
import com.inductiveautomation.ignition.common.tags.model.event.TagChangeListener
import com.mussonindustrial.ignition.embr.common.alarming.SimpleAlarmListener
import com.mussonindustrial.ignition.embr.common.gson.addProperty
import com.mussonindustrial.ignition.embr.common.logging.getLogger
import org.eclipse.jetty.io.EofException
import org.eclipse.jetty.servlets.EventSource
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class TagStreamManager(context: TagStreamGatewayContext) {

    private val logger = this.getLogger()
    private val executionManager = context.executionManager
    private val tagManager = context.tagManager
    private val alarmManager = context.alarmManager
    private val systemTags = context.tagStreamSystemTagsProvider
    private val tagHistoryManager = context.tagHistoryManager
    private val tagHistoryCache = TagHistoryCache()
    private val tagHistoryQueryProvider = { params: TagHistoryQueryParams ->
        val dataset = StreamingDataset()
        tagHistoryManager.queryHistory(params, dataset)
        dataset
    }
    private val sessions = hashMapOf<String, Session>()
    private val tagGson = TagGson.create()

    fun createSession(tagPaths: List<TagPath>, securityContext: SecurityContext): Session {
        val session = Session(tagPaths, securityContext)
        sessions[session.id] = session
        updateMetrics()
        return session
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

    private fun updateMetrics() {
        systemTags.sessionCountConnected = sessions.count { (_, session) -> session.opened.plain }
        systemTags.sessionCountUnconnected = sessions.count { (_, session) -> !session.opened.plain }
    }

    inner class Session(tagPaths: List<TagPath>, val securityContext: SecurityContext): EventSource {
        private val logger = this.getLogger()
        private var emitter: EventSource.Emitter? = null
        val id = UUID.randomUUID().toString()
        val opened = AtomicBoolean(false)

        val tagListeners = tagPaths.withIndex().map { TagListener(it.index, it.value) }
        val tagHistoryClient = TagHistoryClient()

        private val gsonAdapter = JsonSerializer<Session> { _, _, context ->
            JsonObject().apply {
                addProperty("session_id", id)
                add("security_context", context.serialize(securityContext))
                add("tags", context.serialize(tagListeners))
            }
        }
        private val tagListenerGsonAdapter = JsonSerializer<TagListener> { tagListener, _, _ ->  tagListener.toGson() }
        private val gson: Gson = GsonBuilder()
            .registerTypeAdapter(Session::class.java, gsonAdapter)
            .registerTypeAdapter(SecurityContext::class.java, SecurityContext.GsonAdapter())
            .registerTypeAdapter(SecurityLevelConfig::class.java, SecurityLevelConfig.GsonAdapter(true))
            .registerTypeAdapter(TagListener::class.java, tagListenerGsonAdapter)
            .create()
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

        inner class TagHistoryClient() {
            private val messageId = AtomicLong(0)

            private fun startMessage(size: Int): Long {
                val id = messageId.incrementAndGet()
                emitter?.event("tag_history_start", JsonObject().apply {
                    addProperty("id", id)
                    addProperty("size", size)
                }.toString())
                return id
            }

            private fun endMessage(id: Long) {
                emitter?.event("tag_history_end", JsonObject().apply {
                    addProperty("id", id)
                }.toString())
            }

            private fun sendMessage(id: Long, timestamp: Long, values: List<Any>) {
                val v = JsonArray()
                values.forEach { v.add(it as Number) }

                val json = JsonObject().apply {
                    addProperty("id", id)
                    addProperty("t", timestamp)
                    add("v", v)
                }
                emitter?.event("tag_history", json.toString())
            }

            fun queryHistory(params: TagHistoryQueryParams) {
                executionManager.executeOnce {
                    val results = tagHistoryCache.query(tagHistoryQueryProvider, params)
                    val id = startMessage(results.rowCount)
                    for (row in 0 until results.rowCount) {
                        val date = results.getValueAt(row, 0) as Date
                        val values = mutableListOf<Number>()
                        for (col in 1 until results.columnCount) {
                            values.add(results.getValueAt(row, col) as Number)
                        }
                        sendMessage(id, date.time, values)
                    }
                    endMessage(id)
                }
            }
        }

        inner class TagListener(val id: Int, val tagPath: TagPath): TagChangeListener, SimpleAlarmListener {
            val alarmPath: QualifiedPath = QualifiedPath.Builder()
                .setProvider(tagPath.source)
                .setTag(tagPath.toStringPartial())
                .build()
            private var subscribed = AtomicBoolean(false)

            private val serializer = JsonSerializer<TagListener> { tagListener, _, _ ->
                JsonObject().apply {
                    addProperty("tag_id", tagListener.id)
                    addProperty("alarm_path", tagListener.alarmPath.toString())
                    addProperty("tag_path",  tagListener.tagPath.toString())
                }
            }
            private val tagChangeEventSerializer = JsonSerializer<TagChangeEvent> { event, _, _ ->
                (tagGson.toJsonTree(event.value) as JsonObject).apply {
                    addProperty("tag_id", id)
                    add("v", remove("value"))
                    add("t", remove("timestamp"))
                    add("q", remove("quality"))
                }
            }
            private val alarmEventSerializer = JsonSerializer<AlarmEvent> { event, _, _ ->
                JsonObject().apply {
                    addProperty("tag_id", id)
                    addProperty("count", event.count)
                    addProperty("displayPath", event.displayPath.toString())
                    addProperty(
                        "eventData",
                        event.ackData ?: event.activeData ?: event.clearedData ?: EventData()
                    )
                    addProperty("extension", event.extension)
                    addProperty("id", event.id.toString())
                    addProperty("isAcked", event.isAcked)
                    addProperty("isCleared", event.isCleared)
                    addProperty("isShelved", event.isShelved)
                    addProperty("label", event.label)
                    addProperty("name", event.name)
                    addProperty("notes", event.notes)
                    addProperty("priority", event.priority.toString())
                    addProperty("source", event.source.toString())
                    addProperty("state", event.state.toString())
                    addProperty("values", event.values)
                }
            }
            private val gsonAdapter: Gson = GsonBuilder()
                .registerTypeAdapter(TagListener::class.java, serializer)
                .registerTypeAdapter(TagChangeEvent::class.java, tagChangeEventSerializer)
                .registerTypeAdapter(AlarmEvent::class.java, alarmEventSerializer)
                .create()
            fun toGson(): JsonObject = this.gsonAdapter.toJsonTree(this).asJsonObject

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
                    emitter?.event("tag_change", this.gsonAdapter.toJson(event))
                }
            }

            override fun getSecurityContext(): SecurityContext {
                return this@Session.securityContext
            }

            override fun onAlarmEvent(event: AlarmEvent) {
                logger.trace("session: {}, alarmEvent: {}", this@Session.id, event)
                closeOnException {
                    emitter?.event("alarm_event", this.gsonAdapter.toJson(event))
                }
            }
        }


    }
}



