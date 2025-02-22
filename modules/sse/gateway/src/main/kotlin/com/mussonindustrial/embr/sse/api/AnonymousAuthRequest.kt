package com.mussonindustrial.embr.sse.api

import com.inductiveautomation.ignition.common.gson.JsonDeserializationContext
import com.inductiveautomation.ignition.common.gson.JsonElement
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.gson.JsonSerializationContext
import com.inductiveautomation.ignition.common.tags.model.SecurityContext
import com.mussonindustrial.embr.common.gson.JsonSerializable
import com.mussonindustrial.embr.sse.EventStreamGatewayContext
import java.lang.reflect.Type

class AnonymousAuthRequest : AuthRequest {
    override val type: String = Companion.type

    companion object : AuthRequestCompanion<AnonymousAuthRequest> {
        override val type = "anonymous"
        override val gsonAdapter =
            object : JsonSerializable<AnonymousAuthRequest> {
                override fun serialize(
                    request: AnonymousAuthRequest,
                    type: Type,
                    serializationContext: JsonSerializationContext,
                ): JsonElement {
                    return JsonObject().apply { addProperty("type", request.type) }
                }

                override fun deserialize(
                    element: JsonElement,
                    type: Type,
                    deserializationContext: JsonDeserializationContext,
                ): AnonymousAuthRequest {
                    return AnonymousAuthRequest()
                }
            }
    }

    override fun getSecurityContext(context: EventStreamGatewayContext): SecurityContext {
        return SecurityContext.emptyContext()
    }
}
