package com.mussonindustrial.ignition.embr.eventstream.streams

import com.inductiveautomation.ignition.common.Dataset
import com.inductiveautomation.ignition.common.QualifiedPath
import com.inductiveautomation.ignition.common.StreamingDataset
import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.alarming.AlarmEvent
import com.inductiveautomation.ignition.common.alarming.EventData
import com.inductiveautomation.ignition.common.gson.Gson
import com.inductiveautomation.ignition.common.gson.GsonBuilder
import com.inductiveautomation.ignition.common.gson.JsonArray
import com.inductiveautomation.ignition.common.gson.JsonElement
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.gson.JsonSerializer
import com.inductiveautomation.ignition.common.sqltags.history.ReturnFormat
import com.inductiveautomation.ignition.common.sqltags.history.TagHistoryQueryParams
import com.inductiveautomation.ignition.common.sqltags.history.cache.TagHistoryCache
import com.inductiveautomation.ignition.common.tags.config.TagGson
import com.inductiveautomation.ignition.common.tags.model.SecurityContext
import com.inductiveautomation.ignition.common.tags.model.TagPath
import com.inductiveautomation.ignition.common.tags.model.event.TagChangeEvent
import com.inductiveautomation.ignition.common.tags.model.event.TagChangeListener
import com.inductiveautomation.ignition.common.tags.paths.parser.TagPathParser
import com.mussonindustrial.ignition.embr.common.alarming.SimpleAlarmListener
import com.mussonindustrial.ignition.embr.common.gson.SimpleGsonAdapter
import com.mussonindustrial.ignition.embr.common.gson.SimpleJsonSerializable
import com.mussonindustrial.ignition.embr.common.gson.addProperty
import com.mussonindustrial.ignition.embr.common.logging.getLogger
import com.mussonindustrial.ignition.embr.eventstream.EventStreamGatewayContext
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class TagStream : EventStream {
    override val key = Companion.key

    companion object : EventStreamCompanion<TagStream> {
        override val key = "tag"

        override fun get(): TagStream {
            return TagStream()
        }

        val sessions = hashMapOf<String, TagStream>()
    }

    private var context = EventStreamGatewayContext.instance
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
    lateinit var tagListeners: List<TagListener>
    private lateinit var events: Set<TagEvent>

    override fun initialize(props: JsonElement) {
        val json = props.asJsonObject
        logger.trace("initializing with properties: {}", json.toString())

        val tagPaths = json.getAsJsonArray("paths").map { TagPathParser.parse(it.asString) }
        tagListeners = tagPaths.withIndex().map { TagListener(it.index, it.value) }
        events = json.getAsJsonArray("events").map { TagEvent.fromValue(it.asString) }.toSet()
    }

    override fun onCreate(session: EventStreamManager.Session) {
        this.session = session
        sessions[session.id] = this
    }

    override fun onOpen() {
        tagListeners.forEach { it.subscribe() }
    }

    override fun onClose() {
        tagListeners.forEach { it.unsubscribe() }
        sessions.remove(session.id)
    }

    fun isEventEnabled(eventType: TagEvent): Boolean {
        return events.contains(eventType)
    }

    fun queryHistory(params: TagHistoryQueryParams) = tagHistoryClient.queryHistory(params)

    enum class TagEvent(val eventType: String) {
        Change("tag_change"),
        Alarm("tag_alarm"),
        ;

        companion object {
            fun fromValue(value: String): TagEvent =
                when (value) {
                    "tag_change" -> Change
                    "tag_alarm" -> Alarm
                    else -> throw IllegalArgumentException()
                }
        }
    }

    inner class TagListener(val id: Int, val tagPath: TagPath) : TagChangeListener, SimpleAlarmListener, SimpleJsonSerializable {
        private val alarmPath: QualifiedPath =
            QualifiedPath.Builder()
                .setProvider(tagPath.source)
                .setTag(tagPath.toStringPartial())
                .build()

        private var subscribed = AtomicBoolean(false)

        private val serializer =
            JsonSerializer<TagListener> { tagListener, _, _ ->
                JsonObject().apply {
                    addProperty("id", tagListener.id)
                    addProperty("path", tagListener.tagPath.toString())
                }
            }
        private val tagChangeEventSerializer =
            JsonSerializer<TagChangeEvent> { event, _, _ ->
                (tagGson.toJsonTree(event.value) as JsonObject).apply {
                    addProperty("tag_id", id)
                    add("v", remove("value"))
                    add("t", remove("timestamp"))
                    add("q", remove("quality"))
                }
            }
        private val alarmEventSerializer =
            JsonSerializer<AlarmEvent> { event, _, _ ->
                JsonObject().apply {
                    addProperty("tag_id", id)
                    addProperty("count", event.count)
                    addProperty("displayPath", event.displayPath.toString())
                    addProperty(
                        "eventData",
                        event.ackData ?: event.activeData ?: event.clearedData ?: EventData(),
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
        private val gsonAdapter: Gson =
            GsonBuilder()
                .registerTypeAdapter(TagListener::class.java, serializer)
                .registerTypeAdapter(TagChangeEvent::class.java, tagChangeEventSerializer)
                .registerTypeAdapter(AlarmEvent::class.java, alarmEventSerializer)
                .create()

        override fun toGson(): JsonObject = this.gsonAdapter.toJsonTree(this).asJsonObject

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
            context.eventStreamExecutionManager.run {
                session.closeOnException {
                    session.emitEvent("tag_change", this@TagListener.gsonAdapter.toJson(event))
                }
            }
        }

        override fun getSecurityContext(): SecurityContext {
            return session.securityContext
        }

        override fun onAlarmEvent(event: AlarmEvent) {
            logger.trace("session: {}, alarmEvent: {}", session.id, event)
            context.eventStreamExecutionManager.run {
                session.closeOnException {
                    session.emitEvent("tag_alarm", this@TagListener.gsonAdapter.toJson(event))
                }
            }
        }
    }

    private inner class TagHistoryClient() {
        private val messageId = AtomicLong(0)

        private fun startMessage(size: Int): Long {
            val messageId = messageId.incrementAndGet()
            session.emitEvent(
                "tag_history_start",
                JsonObject().apply {
                    addProperty("message_id", messageId)
                    addProperty("size", size)
                }.toString(),
            )
            return messageId
        }

        private fun endMessage(messageId: Long) {
            session.emitEvent(
                "tag_history_end",
                JsonObject().apply {
                    addProperty("message_id", messageId)
                }.toString(),
            )
        }

        private fun sendMessage(
            messageId: Long,
            timestamp: Long,
            values: List<Any>,
        ) {
            val v = JsonArray()
            values.forEach { v.add(it as Number) }

            val json =
                JsonObject().apply {
                    addProperty("message_id", messageId)
                    addProperty("t", timestamp)
                    add("v", v)
                }
            session.emitEvent("tag_history", json.toString())
        }

        private fun sendMessage(
            messageId: Long,
            timestamp: Long,
            tagId: Number,
            value: Number,
        ) {
            val json =
                JsonObject().apply {
                    addProperty("message_id", messageId)
                    addProperty("tag_id", tagId)
                    addProperty("t", timestamp)
                    addProperty("v", value)
                }
            session.emitEvent("tag_history", json.toString())
        }

        fun queryHistory(params: TagHistoryQueryParams) {
            context.eventStreamExecutionManager.executeOnce {
                val results = tagHistoryCache.query(tagHistoryQueryProvider, params)
                when (params.returnFormat) {
                    ReturnFormat.Wide -> sendWideHistory(results)
                    ReturnFormat.Tall -> sendTallHistory(results)
                    else -> {}
                }
            }
        }

        fun sendWideHistory(results: Dataset) {
            val id = startMessage(results.rowCount)
            for (row in 0 until results.rowCount) {
                val date = TypeUtilities.toDate(results.getValueAt(row, 0))
                val values = mutableListOf<Number>()
                for (col in 1 until results.columnCount) {
                    values.add(TypeUtilities.toNumber(results.getValueAt(row, col)))
                }
                sendMessage(id, date.time, values)
            }
            endMessage(id)
        }

        fun sendTallHistory(results: Dataset) {
            val id = startMessage(results.rowCount)
            logger.info(results.columnNames.toString())
            for (row in 0 until results.rowCount) {
                val date = TypeUtilities.toDate(results.getValueAt(row, 3))
                val tagId = TypeUtilities.toInteger(results.getValueAt(row, 0))
                val value = TypeUtilities.toNumber(results.getValueAt(row, 1))
                sendMessage(id, date.time, tagId, value)
            }
            endMessage(id)
        }
    }

    private val gsonAdapter =
        JsonSerializer<TagStream> { emitter, _, serializationContext ->
            JsonObject().apply {
                add("tags", serializationContext.serialize(emitter.tagListeners))
                add("events", JsonArray().apply { emitter.events.forEach { add(it.eventType) } })
            }
        }
    private val gson: Gson =
        GsonBuilder().apply {
            registerTypeAdapter(TagStream::class.java, gsonAdapter)
            registerTypeAdapter(TagListener::class.java, SimpleGsonAdapter<TagListener>())
        }.create()

    override fun toGson(): JsonElement {
        return gson.toJsonTree(this)
    }
}
