package com.mussonindustrial.ignition.embr.tagstream.api

import com.inductiveautomation.ignition.common.gson.*
import com.inductiveautomation.ignition.common.tags.model.SecurityContext
import com.inductiveautomation.ignition.common.user.BasicAuthChallenge
import com.mussonindustrial.ignition.embr.tagstream.TagStreamGatewayContext
import java.lang.reflect.Type

class BasicAuthRequest(val username: String, val password: String): AuthRequest {

    override val type: String = gsonAdapter.type

    companion object {
        val gsonAdapter = object : AuthRequestGsonAdapter<BasicAuthRequest> {
            override val type = "basic"

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

    override fun getSecurityContext(context: TagStreamGatewayContext): SecurityContext {
        val challenge = BasicAuthChallenge(username, password)
        val user = context.userSourceProfile.authenticate(challenge)
        return SecurityContext.fromAuthenticatedUser(user)
    }
}