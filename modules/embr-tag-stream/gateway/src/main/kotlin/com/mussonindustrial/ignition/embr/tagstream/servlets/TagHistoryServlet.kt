package com.mussonindustrial.ignition.embr.tagstream.servlets

import com.inductiveautomation.ignition.common.gson.*
import com.inductiveautomation.ignition.common.util.fromJson
import com.mussonindustrial.ignition.embr.common.logging.getLogger
import com.mussonindustrial.ignition.embr.gateway.api.sendError
import com.mussonindustrial.ignition.embr.gateway.api.sendSuccess
import com.mussonindustrial.ignition.embr.tagstream.EventStreamGatewayContext
import com.mussonindustrial.ignition.embr.tagstream.api.TagHistoryRequest
import com.mussonindustrial.ignition.embr.tagstream.emitters.TagEmitter
import com.mussonindustrial.ignition.embr.tagstream.history.TagStreamHistoryQueryParams
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TagHistoryServlet: HttpServlet() {
    private val logger = this.getLogger()
    private val sessions = TagEmitter.sessions

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        logger.trace("Get request received: {}", request)
        val path = request.requestURI.substring(request.contextPath.length + request.servletPath.length)

        if (path != "") {
            response.sendError("bad path")
            return
        }

        val tagHistoryRequest: TagHistoryRequest
        try {
            val json = JsonParser.parseReader(request.reader).asJsonObject
            logger.trace("Request body: {}", json)
            tagHistoryRequest = TagHistoryRequest.gson.fromJson(json)

        } catch (e: Throwable) {
            logger.warn("Rejecting history request, malformed body.", e)
            response.sendError("malformed body")
            return
        }


        val emitter = sessions[tagHistoryRequest.sessionId]
        if (emitter == null) {
            logger.warn("Request received for an invalid Session ID.")
            response.sendError("invalid session")
            return
        }

        logger.trace("Session {} was found. Requesting history for session.", emitter.session.id)
        response.sendSuccess()
        val params = TagStreamHistoryQueryParams(emitter, tagHistoryRequest)
        emitter.queryHistory(params)
    }
}