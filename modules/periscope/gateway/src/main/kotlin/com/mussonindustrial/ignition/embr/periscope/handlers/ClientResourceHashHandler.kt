package com.mussonindustrial.ignition.embr.periscope.handlers

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.gateway.dataroutes.RequestContext
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.gateway.api.cacheNever
import com.mussonindustrial.embr.gateway.api.sendSuccess
import com.mussonindustrial.embr.perspective.gateway.reflect.PerspectiveRoutes
import com.mussonindustrial.embr.perspective.gateway.routes.SessionRouteHandler
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import jakarta.servlet.http.HttpServletResponse
import kotlin.jvm.optionals.getOrNull

class ClientResourceHashHandler(private val context: PeriscopeGatewayContext) :
    SessionRouteHandler("application/json") {

    private val logger = this.getLoggerEx()

    override fun respond(request: RequestContext, response: HttpServletResponse) {
        logger.trace("Client resource hash request: $request")

        val projectName =
            request.getParameter("project_name")
                ?: return response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing project name.",
                )

        val snapshot =
            context.clientResourceSnapshotManager.find(projectName)
                ?: return response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Project '$projectName' not found.",
                )

        addComponentDefaultsHandler(request, projectName)

        val json = JsonObject()
        if (snapshot.resources.isNotEmpty()) {
            json.addProperty("hash", snapshot.hash)
        }

        response.cacheNever()
        response.sendSuccess(json)
    }

    private fun addComponentDefaultsHandler(request: RequestContext, projectName: String) {
        val session = PerspectiveRoutes.getSession(request.request) ?: return
        val projectSession = session.findProjectSession(projectName).getOrNull() ?: return
        context.componentDefaultsManager.addHandler(projectSession)
    }
}
