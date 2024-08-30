package com.mussonindustrial.embr.eventstream.servlets

import com.inductiveautomation.ignition.common.gson.Gson
import com.inductiveautomation.ignition.common.gson.GsonBuilder
import com.inductiveautomation.ignition.common.gson.JsonParser
import com.inductiveautomation.ignition.common.util.fromJson
import com.mussonindustrial.embr.common.logging.getLogger
import com.mussonindustrial.embr.eventstream.api.TagHistoryRequest
import com.mussonindustrial.embr.eventstream.history.TagStreamHistoryQueryParams
import com.mussonindustrial.embr.eventstream.streams.TagStream
import com.mussonindustrial.embr.gateway.api.sendError
import com.mussonindustrial.embr.gateway.api.sendSuccess
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TagHistoryServlet : HttpServlet() {
    private val logger = this.getLogger()

    companion object {
        val gson: Gson =
            GsonBuilder()
                .registerTypeAdapter(TagHistoryRequest::class.java, TagHistoryRequest.gsonAdapter)
                .create()
    }

    override fun doGet(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        logger.trace("Get request received: {}", request)
        val path =
            request.requestURI.substring(request.contextPath.length + request.servletPath.length)

        if (path != "") {
            response.sendError("bad path")
            return
        }

        val tagHistoryRequest: TagHistoryRequest
        try {
            val json = JsonParser.parseReader(request.reader).asJsonObject
            logger.trace("Request body: {}", json)
            tagHistoryRequest = gson.fromJson(json)
        } catch (e: Throwable) {
            logger.warn("Rejecting history request, malformed body.", e)
            response.sendError("malformed body")
            return
        }

        val emitter = TagStream.sessions[tagHistoryRequest.sessionId]
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
