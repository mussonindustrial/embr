package com.mussonindustrial.ignition.embr.sse.servlets

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.gson.JsonParser
import com.mussonindustrial.ignition.embr.sse.GatewayHook
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.text.Charsets.UTF_8

class TagStreamCreationServlet: HttpServlet() {

    private val logger = LoggerFactory.getLogger("TagStreamCreationServlet")
    private val context = GatewayHook.context
    private val tagStreamManager = GatewayHook.tagStreamManager
    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
        val paths: List<String>
        try {
            val body = JsonParser.parseReader(request.reader)
            paths = body.asJsonObject.getAsJsonArray("tags").map { it.asString }
        } catch (e: Throwable) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "malformed body")
            return
        }

        val id = tagStreamManager.getOrCreateStream(paths).id
        val url = "http://localhost:8088/embr-sse/$id"


        logger.info("Paths received received: $paths")
        logger.info("ID received received: $id")
        logger.info("URL returned: $url")

        val json = JsonObject()
        json.addProperty("url", url)

        response.apply {
            status = HttpServletResponse.SC_OK
            contentType = "application/json"
            characterEncoding = UTF_8.toString()
            writer.println(json.toString())
            writer.close()
        }
    }
}