package com.mussonindustrial.embr.sse.api

import com.inductiveautomation.ignition.common.gson.JsonDeserializationContext
import com.inductiveautomation.ignition.common.gson.JsonElement
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.gson.JsonSerializationContext
import com.mussonindustrial.embr.common.gson.JsonSerializable
import com.mussonindustrial.embr.common.logging.getLogger
import com.mussonindustrial.embr.sse.streams.EventStreamManager
import java.lang.reflect.Type

data class EventStreamSessionRequest(
    val auth: AuthRequest,
    val subscriptionProps: Map<String, JsonElement>,
) {
    companion object {
        val logger = this.getLogger()
        val gsonAdapter =
            object : JsonSerializable<EventStreamSessionRequest> {
                override fun serialize(
                    request: EventStreamSessionRequest,
                    type: Type,
                    serializationContext: JsonSerializationContext,
                ): JsonElement {
                    return JsonObject().apply {
                        add("auth", serializationContext.serialize(request.auth))
                        add(
                            "streams",
                            JsonObject().apply {
                                request.subscriptionProps.forEach { (key, json) ->
                                    this.add(key, json)
                                }
                            },
                        )
                    }
                }

                override fun deserialize(
                    element: JsonElement,
                    type: Type,
                    deserializationContext: JsonDeserializationContext,
                ): EventStreamSessionRequest {
                    val json = element.asJsonObject

                    val auth: AuthRequest =
                        deserializationContext.deserialize(
                            json.get("auth"),
                            AuthRequest::class.java,
                        )

                    val streams = json.getAsJsonObject("streams")
                    val streamKeys = streams.keySet()
                    logger.trace("stream request: {}", streams)

                    return EventStreamSessionRequest(
                        auth,
                        streamKeys.associateWith { streams.get(it) },
                    )
                }
            }
    }

    fun getSessionType(): EventStreamManager.SessionType {
        return when (this.auth) {
            is PerspectiveAuthRequest -> EventStreamManager.SessionType.PERSPECTIVE
            else -> EventStreamManager.SessionType.WEB
        }
    }
}
