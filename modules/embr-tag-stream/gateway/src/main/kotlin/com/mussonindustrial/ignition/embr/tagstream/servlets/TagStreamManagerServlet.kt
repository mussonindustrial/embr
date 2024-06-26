package com.mussonindustrial.ignition.embr.tagstream.servlets

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.gson.JsonParser
import com.inductiveautomation.ignition.common.tags.model.SecurityContext
import com.inductiveautomation.ignition.common.user.BasicAuthChallenge
import com.inductiveautomation.ignition.common.util.asStringOrNull
import com.mussonindustrial.ignition.embr.common.logging.getLogger
import com.mussonindustrial.ignition.embr.tagstream.TagStreamGatewayContext
import java.util.*
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.text.Charsets.UTF_8

class TagStreamManagerServlet: HttpServlet() {

    private val logger = this.getLogger()
    private val context = TagStreamGatewayContext.INSTANCE
    private val tagStreamManager = context.tagStreamManager

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

    data class RequestBody(
        val username: String?,
        val password: String?,
        val perspectiveSessionId: String?,
        val tagPaths: List<String>,
    )

    private fun getPerspectiveSecurityContext(sessionId: String): SecurityContext? {


        return context.perspectiveContext?.run {
            logger.trace("Attempting to find Perspective Session {}...", sessionId)
            val uuid = UUID.fromString(sessionId)

            return this.sessionMonitor.findSession(uuid)
                .map {
                    logger.trace("Perspective Session {} found.", it.sessionId)
                    SecurityContext.fromSecurityLevels(it.webAuthStatus.securityLevels)
                }.orElse(null)
        }
    }

    private fun getUserSecurityContext(username: String?, password: String?): SecurityContext? {
        val challenge = BasicAuthChallenge(username, password)
        val user = context.userSourceProfile.authenticate(challenge)
        return SecurityContext.fromAuthenticatedUser(user)
    }

    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
        logger.trace("Post request received.")
        val path = request.requestURI.substring(request.contextPath.length + request.servletPath.length)

        if (path != "") {
            response.sendError("bad path")
            return
        }

        val body: RequestBody
        try {
            val json = JsonParser.parseReader(request.reader).asJsonObject
            logger.trace("Post request body: {}", json)

            val username = json.get("username")?.asStringOrNull()
            val password = json.get("password")?.asStringOrNull()
            val perspectiveSessionId = json.get("perspective_session_id")?.asStringOrNull()
            val paths = json.getAsJsonArray("tag_paths").map { it.asString }
            body = RequestBody(username, password, perspectiveSessionId, paths)

        } catch (e: Throwable) {
            logger.warn("Rejecting subscription request, malformed body.", e)
            response.sendError("malformed body")
            return
        }

        val securityContext: SecurityContext?
        try {
            if (body.perspectiveSessionId !== null) {
                logger.trace("Perspective session id found in POST request.")
                securityContext = getPerspectiveSecurityContext(body.perspectiveSessionId)

            } else if (body.username !== null) {
                logger.trace("Username/password found in POST request.")
                securityContext = getUserSecurityContext(body.username, body.password)

            } else {
                securityContext = null
            }

        } catch (e: Throwable) {
            logger.warn("Rejecting subscription request, authentication failure.", e)
            response.sendError("auth failure")
            return
        }


        if (securityContext !== null) {
            val session = tagStreamManager.createSession(body.tagPaths, securityContext)
            logger.trace("Authenticated Session {} created: {}", session.id, securityContext)
            response.sendSuccess(session.sessionInfo)
        } else {
            val session = tagStreamManager.createSession(body.tagPaths)
            logger.trace("Anonymous Session {} created.", session.id)
            response.sendSuccess(session.sessionInfo)
        }
    }
}