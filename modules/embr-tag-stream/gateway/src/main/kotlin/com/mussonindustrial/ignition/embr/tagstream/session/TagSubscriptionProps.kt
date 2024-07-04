package com.mussonindustrial.ignition.embr.tagstream.session

import com.inductiveautomation.ignition.common.gson.*
import com.inductiveautomation.ignition.common.tags.model.TagPath
import com.inductiveautomation.ignition.common.tags.paths.parser.TagPathParser
import com.mussonindustrial.ignition.embr.common.gson.JsonSerializable
import com.mussonindustrial.ignition.embr.tagstream.api.TagHistoryRequest
import java.lang.reflect.Type

data class TagSubscriptionProps(val paths: List<TagPath>, val events: Set<TagEvent>) {

    companion object {
        val gsonAdapter = object : JsonSerializable<TagSubscriptionProps> {
            override fun serialize(request: TagSubscriptionProps, type: Type, serializationContext: JsonSerializationContext): JsonElement {
                return JsonObject().apply {
                    add("paths", JsonArray().apply { request.paths.forEach{ add(it.toString()) } })
                    add("events", JsonArray().apply { request.events.forEach{ add(it.eventType) } })
                }
            }

            override fun deserialize(element: JsonElement, type: Type, deserializationContext: JsonDeserializationContext): TagSubscriptionProps {
                val json = element.asJsonObject
                return TagSubscriptionProps(
                    json.getAsJsonArray("paths").map { TagPathParser.parse(it.asString) },
                    json.getAsJsonArray("events").map { TagEvent.fromValue(it.asString) }.toSet()
                )
            }
        }

        val gson: Gson = GsonBuilder()
            .registerTypeAdapter(TagHistoryRequest::class.java, this.gsonAdapter)
            .create()
    }
}