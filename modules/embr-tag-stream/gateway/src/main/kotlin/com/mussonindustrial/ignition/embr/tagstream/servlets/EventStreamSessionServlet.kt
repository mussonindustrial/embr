package com.mussonindustrial.ignition.embr.tagstream.servlets

import com.inductiveautomation.ignition.common.gson.JsonParser
import com.inductiveautomation.ignition.common.util.fromJson
import com.mussonindustrial.ignition.embr.common.logging.getLogger
import com.mussonindustrial.ignition.embr.tagstream.EventStreamGatewayContext
import com.mussonindustrial.ignition.embr.tagstream.api.EventStreamSessionRequest
import com.mussonindustrial.ignition.embr.gateway.api.sendSuccess
import com.mussonindustrial.ignition.embr.gateway.api.sendError
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class EventStreamSessionServlet: HttpServlet() {
    private val logger = this.getLogger()
    private val context = EventStreamGatewayContext.INSTANCE
    private val eventStreamManager = context.eventStreamManager

    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
        logger.trace("Post request received: {}", request)
        val path = request.requestURI.substring(request.contextPath.length + request.servletPath.length)

        if (path != "") {
            response.sendError("bad path")
            return
        }

        val sessionRequest: EventStreamSessionRequest
        try {
            val json = JsonParser.parseReader(request.reader).asJsonObject
            logger.trace("Request body: {}", json)
            sessionRequest = EventStreamSessionRequest.gson.fromJson(json)

            val securityContext = sessionRequest.auth.getSecurityContext(context)

            val session = eventStreamManager.createSession(sessionRequest.subscriptionProps, securityContext)
            logger.trace("Session {} created with security context: {}", session.id, securityContext)
            response.sendSuccess(session.toGson())
            return

        } catch (e: Throwable) {
            logger.warn("Rejecting subscription request, malformed body.", e)
            response.sendError("malformed body")
            return
        }

    }
}