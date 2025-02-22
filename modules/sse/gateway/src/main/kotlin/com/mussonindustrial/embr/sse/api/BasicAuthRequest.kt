package com.mussonindustrial.embr.sse.api

import com.inductiveautomation.ignition.common.gson.JsonDeserializationContext
import com.inductiveautomation.ignition.common.gson.JsonElement
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.gson.JsonSerializationContext
import com.inductiveautomation.ignition.common.tags.model.SecurityContext
import com.inductiveautomation.ignition.common.user.BasicAuthChallenge
import com.mussonindustrial.embr.common.gson.JsonSerializable
import com.mussonindustrial.embr.sse.EventStreamGatewayContext
import java.lang.reflect.Type

class BasicAuthRequest(val username: String, val password: String) : AuthRequest {
    override val type: String = Companion.type

    companion object : AuthRequestCompanion<BasicAuthRequest> {
        override val type = "basic"
        override val gsonAdapter =
            object : JsonSerializable<BasicAuthRequest> {
                override fun serialize(
                    request: BasicAuthRequest,
                    type: Type,
                    serializationContext: JsonSerializationContext,
                ): JsonElement {
                    return JsonObject().apply {
                        addProperty("type", request.type)
                        addProperty("username", request.username)
                        addProperty("password", request.password)
                    }
                }

                override fun deserialize(
                    element: JsonElement,
                    type: Type,
                    deserializationContext: JsonDeserializationContext,
                ): BasicAuthRequest {
                    val json = element.asJsonObject
                    return BasicAuthRequest(
                        json.get("username").asString,
                        json.get("password").asString,
                    )
                }
            }
    }

    override fun getSecurityContext(context: EventStreamGatewayContext): SecurityContext {
        val challenge = BasicAuthChallenge(username, password)
        val user = context.userSourceProfile.authenticate(challenge)
        return SecurityContext.fromAuthenticatedUser(user)
    }
}
