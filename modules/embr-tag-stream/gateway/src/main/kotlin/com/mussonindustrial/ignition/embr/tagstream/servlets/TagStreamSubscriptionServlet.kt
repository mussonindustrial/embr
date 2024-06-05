package com.mussonindustrial.ignition.embr.tagstream.servlets

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.gson.JsonParser
import com.mussonindustrial.ignition.embr.tagstream.TagStreamGatewayHook
import com.mussonindustrial.ignition.embr.tagstream.getLogger
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.text.Charsets.UTF_8

class TagStreamSubscriptionServlet: HttpServlet() {

    private val logger = this.getLogger()
    private val tagStreamManager = TagStreamGatewayHook.context.tagStreamManager
    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
        logger.trace("Post request received.")
        val paths: List<String>
        try {
            val body = JsonParser.parseReader(request.reader)
            logger.trace("Post request body: {}", body)
            paths = body.asJsonObject.getAsJsonArray("tags").map { it.asString }
        } catch (e: Throwable) {
            logger.warn("Rejecting subscription request.", e)
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "malformed body")
            return
        }

        val id = tagStreamManager.openStream(paths).id
        logger.trace("Subscript requested for TagStream: {}", id)

        val json = JsonObject()
        json.addProperty("id", id)

        response.apply {
            status = HttpServletResponse.SC_OK
            contentType = "application/json"
            characterEncoding = UTF_8.toString()
            writer.println(json.toString())
            writer.close()
        }
    }
}