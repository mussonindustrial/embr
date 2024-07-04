package com.mussonindustrial.ignition.embr.tagstream.api

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.gson.*
import com.inductiveautomation.ignition.common.sqltags.history.*
import com.mussonindustrial.ignition.embr.common.gson.JsonSerializable
import java.lang.reflect.Type
import java.util.*

data class TagHistoryRequest(
    val sessionId: String,
    val startDate: Date,
    val endDate: Date,
    val returnSize: Int,
    val aggregationMode: AggregationMode,
) {

    companion object {
        val gsonAdapter = object : JsonSerializable<TagHistoryRequest> {
            override fun serialize(request: TagHistoryRequest, type: Type, serializationContext: JsonSerializationContext): JsonElement {
                return JsonObject().apply {
                    addProperty("session_id",request.sessionId)
                    addProperty("start_date", request.startDate.time)
                    addProperty("end_date", request.endDate.time)
                    addProperty("return_size", request.returnSize)
                    addProperty("aggregation_mode", request.aggregationMode.name)
                }
            }

            override fun deserialize(element: JsonElement, type: Type, deserializationContext: JsonDeserializationContext): TagHistoryRequest {
                val json = element.asJsonObject
                return TagHistoryRequest(
                    json.get("session_id").asString,
                    TypeUtilities.toDate(json.get("start_date")?.asLong),
                    TypeUtilities.toDate(json.get("end_date")?.asLong),
                    json.get("return_size")?.asInt ?: -1,
                    AggregationMode.valueOfCaseInsensitive(json.get("aggregation_mode")?.asString ?: "simpleaverage"),
                )
            }
        }

        val gson: Gson = GsonBuilder()
            .registerTypeAdapter(TagHistoryRequest::class.java, this.gsonAdapter)
            .create()
    }
}