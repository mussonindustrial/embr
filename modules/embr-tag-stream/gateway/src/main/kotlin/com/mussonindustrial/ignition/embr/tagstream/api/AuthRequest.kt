package com.mussonindustrial.ignition.embr.tagstream.api

import com.inductiveautomation.ignition.common.gson.*
import com.inductiveautomation.ignition.common.tags.model.SecurityContext
import com.mussonindustrial.ignition.embr.common.gson.JsonSerializable
import com.mussonindustrial.ignition.embr.tagstream.TagStreamGatewayContext
import java.lang.reflect.Type

sealed interface AuthRequest {

    val type: String

    companion object {
        val gsonAdapter = object : JsonSerializable<AuthRequest> {
            override fun serialize(request: AuthRequest, type: Type, serializationContext: JsonSerializationContext): JsonElement {
                return serializationContext.serialize(request, request::class.java)
            }

            override fun deserialize(element: JsonElement, type: Type, deserializationContext: JsonDeserializationContext): AuthRequest {
                val json = element.asJsonObject
                val clazz = when (json.get("type").asString) {
                    BasicAuthRequest.gsonAdapter.type -> BasicAuthRequest::class.java
                    PerspectiveAuthRequest.gsonAdapter.type -> PerspectiveAuthRequest::class.java
                    AnonymousAuthRequest.gsonAdapter.type -> AnonymousAuthRequest::class.java
                    else -> AnonymousAuthRequest::class.java
                }
                return deserializationContext.deserialize(element, clazz)
            }
        }
    }

    fun getSecurityContext(context: TagStreamGatewayContext): SecurityContext

}

interface AuthRequestGsonAdapter<T: AuthRequest>: JsonSerializable<T> {
    val type: String
}