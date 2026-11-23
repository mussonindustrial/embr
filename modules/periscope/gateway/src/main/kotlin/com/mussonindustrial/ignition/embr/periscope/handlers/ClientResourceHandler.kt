package com.mussonindustrial.ignition.embr.periscope.handlers

import com.inductiveautomation.ignition.common.resourcecollection.ResourcePath
import com.inductiveautomation.ignition.gateway.dataroutes.RequestContext
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.gateway.api.cacheForever
import com.mussonindustrial.embr.gateway.api.cacheNever
import com.mussonindustrial.embr.perspective.gateway.routes.SessionRouteHandler
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import com.mussonindustrial.ignition.embr.periscope.js.ClientResourceImportRewriter
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import com.mussonindustrial.ignition.embr.periscope.utils.findClientResources
import jakarta.servlet.http.HttpServletResponse
import java.net.URLDecoder
import kotlin.jvm.optionals.getOrNull

class ClientResourceHandler(private val context: PeriscopeGatewayContext) :
    SessionRouteHandler("text/javascript") {

    private val logger = this.getLoggerEx()

    override fun respond(request: RequestContext, response: HttpServletResponse) {
        logger.trace("Client resource request: $request")

        val hash =
            request.getParameter("hash")
                ?: return response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing hash.")

        val projectName =
            request.getParameter("project_name")
                ?: return response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing project name.",
                )

        val resourcePath = request.path.substringAfter("$projectName/$hash/")
        val resourceName = URLDecoder.decode(resourcePath, Charsets.UTF_8)
        logger.trace("Project: $projectName, Resource: $resourceName")

        val collection = context.projectManager.findClientResources(projectName)
        val resource =
            collection?.getResource(ResourcePath(ClientResource.type, resourceName))?.getOrNull()

        val clientResource =
            resource?.let(context.clientResourceManager::fromResource)
                ?: return response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Resource '$resourceName' not found.",
                )

        val rewriter = ClientResourceImportRewriter(projectName, hash, resourceName)
        val data = rewriter.rewrite(clientResource.fileContents.compiled)

        response.apply {
            contentType = clientResource.contentType
            characterEncoding = Charsets.UTF_8.name()
            if (context.getClientResourceHash(collection) == hash) {
                cacheForever()
            } else {
                cacheNever()
            }
            writer.use { it.write(data) }
        }
    }
}
