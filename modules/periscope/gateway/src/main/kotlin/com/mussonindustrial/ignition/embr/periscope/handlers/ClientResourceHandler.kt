package com.mussonindustrial.ignition.embr.periscope.handlers

import com.inductiveautomation.ignition.common.resourcecollection.ResourcePath
import com.inductiveautomation.ignition.gateway.dataroutes.RequestContext
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import com.mussonindustrial.ignition.embr.periscope.js.ClientResourceImportRewriter
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import com.mussonindustrial.ignition.embr.periscope.utils.findClientResources
import jakarta.servlet.http.HttpServletResponse
import java.net.URLDecoder
import kotlin.jvm.optionals.getOrNull

class ClientResourceHandler(context: PeriscopeGatewayContext) :
    SessionRouteHandler(context, "text/javascript") {

    val logger = this.getLoggerEx()

    override fun respond(request: RequestContext, response: HttpServletResponse) {
        logger.trace("Client Resource request: $request")

        val hash =
            request.getParameter("hash")
                ?: return response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing hash")

        val projectName =
            request.getParameter("project_name")
                ?: return response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing project name",
                )

        val resourcePath = request.path.substringAfter("$projectName/$hash/")
        val resourceName = URLDecoder.decode(resourcePath, Charsets.UTF_8)
        logger.trace("Project: $projectName, Resource: $resourceName")

        val resource =
            context.projectManager
                .findClientResources(projectName)
                ?.getResource(ResourcePath(ClientResource.type, resourceName))
                ?.getOrNull()

        if (resource == null) {
            response.sendError(
                HttpServletResponse.SC_NOT_FOUND,
                "Resource not found: $resourceName",
            )
            return
        }

        val clientResource = context.clientResourceManager.fromResource(resource)
        if (clientResource == null) {
            response.sendError(
                HttpServletResponse.SC_NOT_FOUND,
                "Resource is not available: $resourceName",
            )
            return
        }

        val rewriter = ClientResourceImportRewriter(projectName, hash)
        val data = rewriter.rewrite(clientResource.fileContents.compiled)

        response.apply {
            contentType = clientResource.contentType
            characterEncoding = Charsets.UTF_8.name()
            cacheForever()
            writer.use { it.write(data) }
        }
    }
}
