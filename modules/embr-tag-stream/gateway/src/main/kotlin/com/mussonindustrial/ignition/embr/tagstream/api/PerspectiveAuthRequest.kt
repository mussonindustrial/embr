package com.mussonindustrial.ignition.embr.tagstream.api

import com.inductiveautomation.ignition.common.gson.*
import com.inductiveautomation.ignition.common.tags.model.SecurityContext
import com.mussonindustrial.ignition.embr.common.logging.getLogger
import com.mussonindustrial.ignition.embr.tagstream.TagStreamGatewayContext
import java.lang.reflect.Type
import java.util.*

class PerspectiveAuthRequest(val sessionId: String): AuthRequest {

    private val logger = this.getLogger()
    override val type: String = gsonAdapter.type

    companion object {
        val gsonAdapter = object : AuthRequestGsonAdapter<PerspectiveAuthRequest> {
            override val type = "perspective"

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

    override fun getSecurityContext(context: TagStreamGatewayContext): SecurityContext {
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