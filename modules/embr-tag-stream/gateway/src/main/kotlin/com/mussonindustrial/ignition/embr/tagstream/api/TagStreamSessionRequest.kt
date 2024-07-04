package com.mussonindustrial.ignition.embr.tagstream.api

import com.inductiveautomation.ignition.common.gson.*
import com.mussonindustrial.ignition.embr.common.gson.JsonSerializable
import com.mussonindustrial.ignition.embr.tagstream.session.TagSubscriptionProps
import java.lang.reflect.Type

data class TagStreamSessionRequest(val auth: AuthRequest, val tags: TagSubscriptionProps?) {

    companion object {
        val gsonAdapter = object : JsonSerializable<TagStreamSessionRequest> {
            override fun serialize(request: TagStreamSessionRequest, type: Type, serializationContext: JsonSerializationContext): JsonElement {
                return JsonObject().apply {
                    add("auth", serializationContext.serialize(request.auth))
                    add("tags", serializationContext.serialize(request.tags))
                }
            }

            override fun deserialize(element: JsonElement, type: Type, deserializationContext: JsonDeserializationContext): TagStreamSessionRequest {
                val json = element.asJsonObject
                return TagStreamSessionRequest(
                    deserializationContext.deserialize(json.get("auth"), AuthRequest::class.java),
                    deserializationContext.deserialize(json.get("tags"), TagSubscriptionProps::class.java),
                )
            }
        }

        val gson: Gson = GsonBuilder()
            .registerTypeAdapter(TagStreamSessionRequest::class.java, this.gsonAdapter)
            .registerTypeAdapter(AuthRequest::class.java, AuthRequest.gsonAdapter)
            .registerTypeAdapter(AnonymousAuthRequest::class.java, AnonymousAuthRequest.gsonAdapter)
            .registerTypeAdapter(BasicAuthRequest::class.java, BasicAuthRequest.gsonAdapter)
            .registerTypeAdapter(PerspectiveAuthRequest::class.java, PerspectiveAuthRequest.gsonAdapter)
            .registerTypeAdapter(TagSubscriptionProps::class.java, TagSubscriptionProps.gsonAdapter)
            .create()
    }
}