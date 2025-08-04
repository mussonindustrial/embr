package com.mussonindustrial.embr.sse.servlets

import com.inductiveautomation.ignition.common.gson.Gson
import com.inductiveautomation.ignition.common.gson.GsonBuilder
import com.inductiveautomation.ignition.common.gson.JsonParser
import com.inductiveautomation.ignition.common.util.fromJson
import com.mussonindustrial.embr.common.logging.getLogger
import com.mussonindustrial.embr.gateway.api.sendError
import com.mussonindustrial.embr.gateway.api.sendSuccess
import com.mussonindustrial.embr.sse.EventStreamGatewayContext
import com.mussonindustrial.embr.sse.api.AnonymousAuthRequest
import com.mussonindustrial.embr.sse.api.AuthRequest
import com.mussonindustrial.embr.sse.api.BasicAuthRequest
import com.mussonindustrial.embr.sse.api.EventStreamSessionRequest
import com.mussonindustrial.embr.sse.api.PerspectiveAuthRequest
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class EventStreamSessionServlet : HttpServlet() {
    private val logger = this.getLogger()
    private val context = EventStreamGatewayContext.instance
    private val eventStreamManager = context.eventStreamManager

    companion object {
        val gson: Gson =
            GsonBuilder()
                .registerTypeAdapter(
                    EventStreamSessionRequest::class.java,
                    EventStreamSessionRequest.gsonAdapter,
                )
                .registerTypeAdapter(AuthRequest::class.java, AuthRequest.gsonAdapter)
                .registerTypeAdapter(
                    AnonymousAuthRequest::class.java,
                    AnonymousAuthRequest.gsonAdapter,
                )
                .registerTypeAdapter(BasicAuthRequest::class.java, BasicAuthRequest.gsonAdapter)
                .registerTypeAdapter(
                    PerspectiveAuthRequest::class.java,
                    PerspectiveAuthRequest.gsonAdapter,
                )
                .create()
    }

    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
        logger.trace("Post request received: {}", request)
        val path =
            request.requestURI.substring(request.contextPath.length + request.servletPath.length)

        if (path != "") {
            response.sendError("bad path")
            return
        }

        val sessionRequest: EventStreamSessionRequest
        try {
            val json = JsonParser.parseReader(request.reader).asJsonObject
            logger.trace("Request body: {}", json)
            sessionRequest = gson.fromJson(json)

            val subscriptionProps = sessionRequest.subscriptionProps
            val securityContext = sessionRequest.auth.getSecurityContext(context)
            val sessionType = sessionRequest.getSessionType()

            val session =
                eventStreamManager.createSession(subscriptionProps, securityContext, sessionType)
            logger.trace(
                "Session {} created with security context: {}",
                session.id,
                securityContext,
            )
            response.sendSuccess(session.toGson())
            return
        } catch (e: Throwable) {
            logger.warn("Rejecting subscription request, malformed body.", e)
            response.sendError("malformed body")
            return
        }
    }
}
