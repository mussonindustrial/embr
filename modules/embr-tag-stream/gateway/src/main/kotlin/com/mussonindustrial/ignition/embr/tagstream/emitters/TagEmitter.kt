package com.mussonindustrial.ignition.embr.tagstream.emitters

import com.inductiveautomation.ignition.common.QualifiedPath
import com.inductiveautomation.ignition.common.StreamingDataset
import com.inductiveautomation.ignition.common.alarming.AlarmEvent
import com.inductiveautomation.ignition.common.alarming.EventData
import com.inductiveautomation.ignition.common.gson.*
import com.inductiveautomation.ignition.common.sqltags.history.TagHistoryQueryParams
import com.inductiveautomation.ignition.common.sqltags.history.cache.TagHistoryCache
import com.inductiveautomation.ignition.common.tags.config.TagGson
import com.inductiveautomation.ignition.common.tags.model.SecurityContext
import com.inductiveautomation.ignition.common.tags.model.TagPath
import com.inductiveautomation.ignition.common.tags.model.event.TagChangeEvent
import com.inductiveautomation.ignition.common.tags.model.event.TagChangeListener
import com.inductiveautomation.ignition.common.tags.paths.parser.TagPathParser
import com.mussonindustrial.ignition.embr.common.alarming.SimpleAlarmListener
import com.mussonindustrial.ignition.embr.common.gson.addProperty
import com.mussonindustrial.ignition.embr.common.logging.getLogger
import com.mussonindustrial.ignition.embr.tagstream.EventStreamGatewayContext
import com.mussonindustrial.ignition.embr.tagstream.EventStreamManager
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class TagEmitter : EventEmitter {

    override val key = KEY
    companion object {
        const val KEY = "tags"
        val gsonAdapter = JsonSerializer<TagEmitter> { eventEmitter, _, _ ->
            eventEmitter.gson.toJsonTree(eventEmitter)
        }
        val sessions = hashMapOf<String, TagEmitter>()
    }

    private var context = EventStreamGatewayContext.INSTANCE
    private val logger = this.getLogger()

    private val alarmManager = context.alarmManager
    private val tagManager = context.tagManager
    private val tagGson = TagGson.create()
    private val tagHistoryManager = context.tagHistoryManager
    private val tagHistoryCache = TagHistoryCache()
    private val tagHistoryClient = TagHistoryClient()
    private val tagHistoryQueryProvider = { params: TagHistoryQueryParams ->
        val dataset = StreamingDataset()
        tagHistoryManager.queryHistory(params, dataset)
        dataset
    }

    lateinit var session: EventStreamManager.Session
    lateinit var listeners: List<TagListener>
    private lateinit var events: Set<TagEvent>

    override fun initialize(props: JsonElement) {
        val json = props.asJsonObject

        val tagPaths = json.getAsJsonArray("paths").map { TagPathParser.parse(it.asString) }
        listeners = tagPaths.withIndex().map { TagListener(it.index, it.value) }
        events = json.getAsJsonArray("events").map { TagEvent.fromValue(it.asString) }.toSet()
    }

    override fun onCreation(session: EventStreamManager.Session) {
        this.session = session
        sessions[session.id] = this
    }

    override fun onOpen() {
        listeners.forEach { it.subscribe() }
    }

    override fun onClose() {
        listeners.forEach { it.unsubscribe() }
        sessions.remove(session.id)
    }

    fun isEventEnabled(eventType: TagEvent): Boolean {
        return events.contains(eventType)
    }

    fun queryHistory(params: TagHistoryQueryParams) = tagHistoryClient.queryHistory(params)

    enum class TagEvent(val eventType: String) {
        Change("tag_change"),
        Alarm("tag_alarm");

        companion object {
            fun fromValue(value: String): TagEvent = when (value) {
                "tag_change" -> Change
                "tag_alarm" -> Alarm
                else -> throw IllegalArgumentException()
            }
        }
    }

    inner class TagListener(val id: Int, val tagPath: TagPath): TagChangeListener, SimpleAlarmListener {
        private val alarmPath: QualifiedPath = QualifiedPath.Builder()
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

        fun subscribe() {
            logger.trace("session: {}, subscribing to {} events.", session.id, tagPath)
            subscribed.set(true)
            if (isEventEnabled(TagEvent.Change)) tagManager.subscribeAsync(tagPath, this)
            if (isEventEnabled(TagEvent.Alarm)) alarmManager.addListener(alarmPath, this)
        }

        fun unsubscribe() {
            if (subscribed.getAndSet(false)) {
                logger.trace("session: {}, unsubscribing from: {}", session.id, tagPath)
                if (isEventEnabled(TagEvent.Change)) tagManager.unsubscribeAsync(tagPath, this)
                if (isEventEnabled(TagEvent.Alarm)) alarmManager.removeListener(alarmPath, this)
            }
        }

        override fun tagChanged(event: TagChangeEvent) {
            logger.trace("session: {}, tagChanged: {}", session.id, event)
            session.closeOnException {
                session.emitEvent("tag_change", this.gsonAdapter.toJson(event))
            }
        }

        override fun getSecurityContext(): SecurityContext {
            return session.securityContext
        }

        override fun onAlarmEvent(event: AlarmEvent) {
            logger.trace("session: {}, alarmEvent: {}", session.id, event)
            session.closeOnException {
                session.emitEvent("tag_alarm", this.gsonAdapter.toJson(event))
            }
        }
    }

    private inner class TagHistoryClient() {
        private val messageId = AtomicLong(0)

        private fun startMessage(size: Int): Long {
            val id = messageId.incrementAndGet()
            session.emitEvent("tag_history_start", JsonObject().apply {
                addProperty("id", id)
                addProperty("size", size)
            }.toString())
            return id
        }

        private fun endMessage(id: Long) {
            session.emitEvent("tag_history_end", JsonObject().apply {
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
            session.emitEvent("tag_history", json.toString())
        }

        fun queryHistory(params: TagHistoryQueryParams) {
            context.executionManager.executeOnce {
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

    private val gsonAdapter = JsonSerializer<TagEmitter> { emitter, _, serializationContext ->
        JsonObject().apply {
            add("listeners", serializationContext.serialize(emitter.listeners))
            add("events", JsonArray().apply { emitter.events.forEach { add(it.eventType) } })
        }
    }
    private val tagListenerGsonAdapter = JsonSerializer<TagListener> { tagListener, _, _ ->  tagListener.toGson() }
    private val gson: Gson = GsonBuilder().apply {
        registerTypeAdapter(TagEmitter::class.java, gsonAdapter)
        registerTypeAdapter(TagListener::class.java, tagListenerGsonAdapter)
    }.create()

}