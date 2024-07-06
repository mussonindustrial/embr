package com.mussonindustrial.ignition.embr.tagstream.api

import com.inductiveautomation.ignition.common.gson.*
import com.mussonindustrial.ignition.embr.common.gson.JsonSerializable
import java.lang.reflect.Type

data class EventStreamSessionRequest(val auth: AuthRequest, val subscriptionProps: Map<String, JsonElement>) {

    companion object {
        private val gsonAdapter = object : JsonSerializable<EventStreamSessionRequest> {
            override fun serialize(request: EventStreamSessionRequest, type: Type, serializationContext: JsonSerializationContext): JsonElement {
                return JsonObject().apply {
                    add("auth", serializationContext.serialize(request.auth))
                    request.subscriptionProps.forEach { (key, json) ->
                        add(key, json)
                    }
                }
            }

            override fun deserialize(element: JsonElement, type: Type, deserializationContext: JsonDeserializationContext): EventStreamSessionRequest {
                val json = element.asJsonObject

                val auth: AuthRequest = deserializationContext.deserialize(json.get("auth"), AuthRequest::class.java)

                val keys = json.keySet()
                keys.remove("auth")
                val subscriptionProps = keys.associateWith { json.get(it) }

                return EventStreamSessionRequest(
                    auth,
                    subscriptionProps,
                )
            }
        }

        val gson: Gson = GsonBuilder()
            .registerTypeAdapter(EventStreamSessionRequest::class.java, this.gsonAdapter)
            .registerTypeAdapter(AuthRequest::class.java, AuthRequest.gsonAdapter)
            .registerTypeAdapter(AnonymousAuthRequest::class.java, AnonymousAuthRequest.gsonAdapter)
            .registerTypeAdapter(BasicAuthRequest::class.java, BasicAuthRequest.gsonAdapter)
            .registerTypeAdapter(PerspectiveAuthRequest::class.java, PerspectiveAuthRequest.gsonAdapter)
            .create()
    }
}