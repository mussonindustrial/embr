package com.mussonindustrial.ignition.embr.tagstream.api

import com.inductiveautomation.ignition.common.Path
import com.inductiveautomation.ignition.common.QualifiedPathUtils
import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.gson.*
import com.inductiveautomation.ignition.common.sqltags.history.*
import com.inductiveautomation.ignition.common.util.TimeUnits
import com.mussonindustrial.ignition.embr.common.gson.JsonSerializable
import java.lang.reflect.Type
import java.util.*

class TagHistoryRequest(
    private val paths: List<Path>,
    private val startDate: Date,
    private val endDate: Date,
    private val intervalLength: Int,
    private val intervalUnits: TimeUnits,
    private val aggregationMode: AggregationMode,
    private val aliases: List<String>?,
    private val columnAggregationModes: List<Aggregate>?
): TagHistoryQueryParams by BasicTagHistoryQueryParams(paths, startDate, endDate, intervalLength, intervalUnits, aggregationMode, ReturnFormat.Wide, aliases, columnAggregationModes) {

    companion object {
        val gsonSerializer = object : JsonSerializable<TagHistoryRequest> {
            override fun serialize(request: TagHistoryRequest, type: Type, serializationContext: JsonSerializationContext): JsonElement {
                return JsonObject().apply {
                    add("paths", JsonArray().apply { request.paths.forEach { add(it.toString()) } })
                    addProperty("start_date", request.startDate.time)
                    addProperty("end_date", request.endDate.time)
                    addProperty("interval_length", request.intervalLength)
                    addProperty("aggregation_mode", request.aggregationMode.name)
                    add("aliases", JsonArray().apply { request.aliases?.forEach { add(it) } })
                    add("column_aggregation_modes", JsonArray().apply { request.columnAggregationModes?.forEach { add(it.name) } })
                }
            }

            override fun deserialize(element: JsonElement, type: Type, deserializationContext: JsonDeserializationContext): TagHistoryRequest {
                val json = element.asJsonObject
                return TagHistoryRequest(
                    json.getAsJsonArray("paths").map { QualifiedPathUtils.toPathFromHistoricalString(it.asString) },
                    TypeUtilities.toDate(json.get("start_date").asLong),
                    TypeUtilities.toDate(json.get("end_date").asLong),
                    TypeUtilities.toInteger(json.get("interval_length").asInt),
                    TimeUnits.MS,
                    AggregationMode.valueOfCaseInsensitive(json.get("aggregation_mode").asString),
                    json.getAsJsonArray("aliases").map { it.asString },
                    json.getAsJsonArray("column_aggregation_modes").map { AggregationMode.valueOfCaseInsensitive(it.asString) },
                )
            }
        }

        val gsonAdapter: Gson = GsonBuilder()
            .registerTypeAdapter(TagHistoryRequest::class.java, gsonSerializer)
            .create()
    }
}