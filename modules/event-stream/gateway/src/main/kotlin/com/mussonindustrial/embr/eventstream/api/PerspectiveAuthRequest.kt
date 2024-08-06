package com.mussonindustrial.embr.eventstream.api

import com.inductiveautomation.ignition.common.gson.JsonDeserializationContext
import com.inductiveautomation.ignition.common.gson.JsonElement
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.gson.JsonSerializationContext
import com.inductiveautomation.ignition.common.tags.model.SecurityContext
import com.mussonindustrial.embr.common.gson.JsonSerializable
import com.mussonindustrial.embr.common.logging.getLogger
import com.mussonindustrial.embr.eventstream.EventStreamGatewayContext
import java.lang.reflect.Type
import java.util.UUID

class PerspectiveAuthRequest(val sessionId: String) : AuthRequest {
    private val logger = this.getLogger()
    override val type: String = Companion.type

    companion object : AuthRequestCompanion<PerspectiveAuthRequest> {
        override val type = "perspective"
        override val gsonAdapter =
            object : JsonSerializable<PerspectiveAuthRequest> {
                override fun serialize(
                    request: PerspectiveAuthRequest,
                    type: Type,
                    serializationContext: JsonSerializationContext,
                ): JsonElement {
                    return JsonObject().apply {
                        addProperty("type", request.type)
                        addProperty("session_id", request.sessionId)
                    }
                }

                override fun deserialize(
                    element: JsonElement,
                    type: Type,
                    deserializationContext: JsonDeserializationContext,
                ): PerspectiveAuthRequest {
                    val json = element.asJsonObject
                    return PerspectiveAuthRequest(
                        json.get("session_id").asString,
                    )
                }
            }
    }

    override fun getSecurityContext(context: EventStreamGatewayContext): SecurityContext {
        return context.perspectiveContext?.run {
            logger.trace("Attempting to find Perspective Session {}...", sessionId)
            return sessionMonitor.findSession(UUID.fromString(sessionId))
                .map {
                    logger.trace("Perspective Session {} found.", it.sessionId)
                    SecurityContext.fromSecurityLevels(it.webAuthStatus.securityLevels)
                }.orElse(null)
        } ?: SecurityContext.emptyContext()
    }
}
