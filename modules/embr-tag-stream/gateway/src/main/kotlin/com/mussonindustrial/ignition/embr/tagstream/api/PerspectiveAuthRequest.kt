package com.mussonindustrial.ignition.embr.tagstream.api

import com.inductiveautomation.ignition.common.gson.*
import com.inductiveautomation.ignition.common.tags.model.SecurityContext
import com.mussonindustrial.ignition.embr.common.gson.JsonSerializable
import com.mussonindustrial.ignition.embr.common.logging.getLogger
import com.mussonindustrial.ignition.embr.tagstream.TagStreamGatewayContext
import java.lang.reflect.Type
import java.util.*

data class PerspectiveAuthRequest(val sessionId: String): AuthRequest {
    private val logger = this.getLogger()

    override val type = "perspective"

    companion object {
        val gsonSerializer = object : JsonSerializable<PerspectiveAuthRequest> {
            override fun serialize(request: PerspectiveAuthRequest, type: Type, serializationContext: JsonSerializationContext): JsonElement {
                return JsonObject().apply {
                    addProperty("type", request.type)
                    addProperty("session_id", request.sessionId)
                }
            }

            override fun deserialize(element: JsonElement, type: Type, deserializationContext: JsonDeserializationContext): PerspectiveAuthRequest {
                val json = element.asJsonObject
                return PerspectiveAuthRequest(
                    json.get("session_id").asString,
                )
            }
        }
    }

    override fun getSecurityContext(context: TagStreamGatewayContext): SecurityContext? {
        return context.perspectiveContext?.run {
            logger.trace("Attempting to find Perspective Session {}...", sessionId)
            val uuid = UUID.fromString(sessionId)

            return this.sessionMonitor.findSession(uuid)
                .map {
                    logger.trace("Perspective Session {} found.", it.sessionId)
                    SecurityContext.fromSecurityLevels(it.webAuthStatus.securityLevels)
                }.orElse(null)
        }
    }
}