package com.mussonindustrial.ignition.embr.tagstream.api

import com.inductiveautomation.ignition.common.gson.*
import com.inductiveautomation.ignition.common.tags.model.SecurityContext
import com.inductiveautomation.ignition.common.user.BasicAuthChallenge
import com.mussonindustrial.ignition.embr.common.gson.JsonSerializable
import com.mussonindustrial.ignition.embr.tagstream.TagStreamGatewayContext
import java.lang.reflect.Type

data class BasicAuthRequest(val username: String, val password: String): AuthRequest {

    override val type = "basic"

    companion object {
        val gsonSerializer = object : JsonSerializable<BasicAuthRequest> {
            override fun serialize(request: BasicAuthRequest, type: Type, serializationContext: JsonSerializationContext): JsonElement {
                return JsonObject().apply {
                    addProperty("type", request.type)
                    addProperty("username", request.username)
                    addProperty("password", request.password)
                }
            }

            override fun deserialize(element: JsonElement, type: Type, deserializationContext: JsonDeserializationContext): BasicAuthRequest {
                val json = element.asJsonObject
                return BasicAuthRequest(
                    json.get("username").asString,
                    json.get("password").asString,
                )
            }
        }
    }

    override fun getSecurityContext(context: TagStreamGatewayContext): SecurityContext? {
        val challenge = BasicAuthChallenge(username, password)
        val user = context.userSourceProfile.authenticate(challenge)
        return SecurityContext.fromAuthenticatedUser(user)
    }
}