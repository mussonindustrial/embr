package com.mussonindustrial.ignition.embr.tagstream.api

import com.inductiveautomation.ignition.common.gson.*
import com.inductiveautomation.ignition.common.tags.model.SecurityContext
import com.mussonindustrial.ignition.embr.common.gson.JsonSerializable
import com.mussonindustrial.ignition.embr.tagstream.TagStreamGatewayContext
import java.lang.reflect.Type

sealed interface AuthRequest {

    val type: String

    companion object {
        val gsonSerializer = object : JsonSerializable<AuthRequest> {
            override fun serialize(request: AuthRequest, type: Type, serializationContext: JsonSerializationContext): JsonElement {
                return when (request::class.java) {
                    BasicAuthRequest::class.java -> serializationContext.serialize(request, BasicAuthRequest::class.java)
                    PerspectiveAuthRequest::class.java -> serializationContext.serialize(request, PerspectiveAuthRequest::class.java)
                    AnonymousAuthRequest::class.java -> serializationContext.serialize(request, PerspectiveAuthRequest::class.java)
                    else -> serializationContext.serialize(request, AnonymousAuthRequest::class.java)
                }
            }

            override fun deserialize(element: JsonElement, type: Type, deserializationContext: JsonDeserializationContext): AuthRequest {
                val json = element.asJsonObject
                return when (json.get("type").asString) {
                    "basic" -> deserializationContext.deserialize(element, BasicAuthRequest::class.java)
                    "perspective" -> deserializationContext.deserialize(element, PerspectiveAuthRequest::class.java)
                    "anonymous" -> deserializationContext.deserialize(element, AnonymousAuthRequest::class.java)
                    else -> deserializationContext.deserialize(element, AnonymousAuthRequest::class.java)
                }
            }
        }

        val gsonAdapter: Gson = GsonBuilder()
            .registerTypeAdapter(BasicAuthRequest::class.java, BasicAuthRequest.gsonSerializer)
            .registerTypeAdapter(PerspectiveAuthRequest::class.java, PerspectiveAuthRequest.gsonSerializer)
            .registerTypeAdapter(AnonymousAuthRequest::class.java, AnonymousAuthRequest.gsonSerializer)
            .create()
    }

    fun getSecurityContext(context: TagStreamGatewayContext): SecurityContext

}