package com.mussonindustrial.ignition.embr.tagstream.api

import com.inductiveautomation.ignition.common.gson.JsonDeserializationContext
import com.inductiveautomation.ignition.common.gson.JsonElement
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.gson.JsonSerializationContext
import com.inductiveautomation.ignition.common.tags.model.SecurityContext
import com.mussonindustrial.ignition.embr.tagstream.EventStreamGatewayContext
import java.lang.reflect.Type

class AnonymousAuthRequest: AuthRequest {

    override val type: String = gsonAdapter.type

    companion object {
        val gsonAdapter = object : AuthRequestGsonAdapter<AnonymousAuthRequest> {
            override val type = "anonymous"

            override fun serialize(request: AnonymousAuthRequest, type: Type, serializationContext: JsonSerializationContext): JsonElement {
                return JsonObject().apply {
                    addProperty("type", request.type)
                }
            }

            override fun deserialize(element: JsonElement, type: Type, deserializationContext: JsonDeserializationContext): AnonymousAuthRequest {
                return AnonymousAuthRequest()
            }
        }

    }

    override fun getSecurityContext(context: EventStreamGatewayContext): SecurityContext {
        return SecurityContext.emptyContext()
    }
}