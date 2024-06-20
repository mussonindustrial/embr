package com.mussonindustrial.ignition.embr.tagstream.servlets

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.gson.JsonParser
import com.mussonindustrial.ignition.embr.tagstream.TagStreamGatewayHook
import com.mussonindustrial.ignition.embr.tagstream.getLogger
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.text.Charsets.UTF_8

class TagStreamManagerServlet: HttpServlet() {

    private val logger = this.getLogger()
    private val tagStreamManager = TagStreamGatewayHook.context.tagStreamManager

    private fun <T: HttpServletResponse> T.sendSuccess(data: JsonObject) {
        val json = JsonObject()
        json.addProperty("status", "success")
        json.add("data", data)

        apply {
            status = HttpServletResponse.SC_OK
            contentType = "application/json"
            characterEncoding = UTF_8.toString()
            writer.println(json.toString())
            writer.close()
        }
    }

    private fun <T: HttpServletResponse> T.sendError(message: String) {
        val json = JsonObject()
        json.addProperty("status", "error")
        json.addProperty("message", message)

        apply {
            status = HttpServletResponse.SC_BAD_REQUEST
            contentType = "application/json"
            characterEncoding = UTF_8.toString()
            writer.println(json.toString())
            writer.close()
        }
    }

    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
        logger.trace("Post request received.")
        val path = request.requestURI.substring(request.contextPath.length + request.servletPath.length)

        if (path != "") {
            response.sendError("bad path")
            return
        }

        val paths: List<String>
        try {
            val body = JsonParser.parseReader(request.reader)
            logger.trace("Post request body: {}", body)
            paths = body.asJsonObject.getAsJsonArray("tagPaths").map { it.asString }
        } catch (e: Throwable) {
            logger.warn("Rejecting subscription request.", e)
            response.sendError("malformed body")
            return
        }

        val session = tagStreamManager.createSession(paths)
        logger.trace("Session {} created.", session.id)

        response.sendSuccess(session.asGson())
    }
}