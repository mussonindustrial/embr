package com.mussonindustrial.ignition.embr.periscope.handlers

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.gateway.dataroutes.RequestContext
import com.inductiveautomation.perspective.gateway.comm.Routes
import com.inductiveautomation.perspective.gateway.session.InternalSession
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.gateway.api.sendSuccess
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import com.mussonindustrial.ignition.embr.periscope.utils.findClientResources
import jakarta.servlet.http.HttpServletResponse
import kotlin.jvm.optionals.getOrNull

class ClientResourceHashHandler(context: PeriscopeGatewayContext) :
    SessionRouteHandler(context, "application/json") {

    val logger = this.getLoggerEx()

    override fun respond(request: RequestContext, response: HttpServletResponse) {
        logger.trace("Client Resource hash request: $request")

        val projectName = request.getParameter("project_name")
        if (projectName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing project name")
            return
        }

        val collection = context.projectManager.findClientResources(projectName)
        if (collection == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found: $projectName")
            return
        }

        val projectSession = findProjectSession(request, projectName)
        if (projectSession != null) {
            context.componentDefaults.listen(projectSession)
        }

        val json = JsonObject()
        if (collection.getResourcesOfType(ClientResource.type).isNotEmpty()) {
            json.addProperty("hash", context.clientResourceHash(collection))
        }

        response.cacheNever()
        response.sendSuccess(json)
    }

    private fun findProjectSession(request: RequestContext, projectName: String): InternalSession? {
        val session = Routes.getSession(request.request).getOrNull() ?: return null
        return session.findProjectSession(projectName).getOrNull()
    }
}
