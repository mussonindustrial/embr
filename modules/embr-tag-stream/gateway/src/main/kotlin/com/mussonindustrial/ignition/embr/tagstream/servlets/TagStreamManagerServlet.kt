package com.mussonindustrial.ignition.embr.tagstream.servlets

import com.inductiveautomation.ignition.common.gson.JsonParser
import com.inductiveautomation.ignition.common.tags.model.SecurityContext
import com.inductiveautomation.ignition.common.util.fromJson
import com.mussonindustrial.ignition.embr.common.logging.getLogger
import com.mussonindustrial.ignition.embr.tagstream.TagStreamGatewayContext
import com.mussonindustrial.ignition.embr.tagstream.api.TagStreamSessionRequest
import com.mussonindustrial.ignition.embr.gateway.api.sendSuccess
import com.mussonindustrial.ignition.embr.gateway.api.sendError
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TagStreamManagerServlet: HttpServlet() {

    private val logger = this.getLogger()
    private val context = TagStreamGatewayContext.INSTANCE
    private val tagStreamManager = context.tagStreamManager

    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
        logger.trace("Post request received: {}", request)
        val path = request.requestURI.substring(request.contextPath.length + request.servletPath.length)

        if (path != "") {
            response.sendError("bad path")
            return
        }

        val sessionRequest: TagStreamSessionRequest
        try {
            val json = JsonParser.parseReader(request.reader).asJsonObject
            logger.trace("Request body: {}", json)
            sessionRequest = TagStreamSessionRequest.gsonAdapter.fromJson(json)

        } catch (e: Throwable) {
            logger.warn("Rejecting subscription request, malformed body.", e)
            response.sendError("malformed body")
            return
        }

        val securityContext = sessionRequest.auth.getSecurityContext(context)
        val session = tagStreamManager.createSession(sessionRequest.tagPaths, securityContext)
        logger.trace("Session {} created with security context: {}", session.id, securityContext)

        response.sendSuccess(session.toGson())
    }
}