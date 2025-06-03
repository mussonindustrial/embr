package com.mussonindustrial.ignition.embr.periscope.handlers

import com.inductiveautomation.ignition.common.project.resource.ResourcePath
import com.inductiveautomation.ignition.gateway.dataroutes.HttpMethod
import com.inductiveautomation.ignition.gateway.dataroutes.RequestContext
import com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup
import com.inductiveautomation.ignition.gateway.dataroutes.RouteHandler
import com.inductiveautomation.perspective.gateway.api.SessionScope
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import com.mussonindustrial.ignition.embr.periscope.js.ClientResourceImportRewriter
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import java.net.URLDecoder
import java.util.EnumSet
import javax.servlet.http.HttpServletResponse

class ClientResourceHandler(val context: PeriscopeGatewayContext) : RouteHandler {

    val logger = this.getLoggerEx()

    fun mount(mounter: RouteGroup.RouteMounter) {
        mounter
            .method(HttpMethod.GET)
            .type("text/javascript")
            .restrict(context.requireSession(EnumSet.allOf(SessionScope::class.java)))
            .handler(this)
            .mount()
    }

    override fun handle(request: RequestContext, response: HttpServletResponse) {
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

        val project = context.projectManager.getProject(projectName).orElse(null)
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found: $projectName")
            return
        }

        val resource =
            project.getResource(ResourcePath(ClientResource.type, resourceName)).orElse(null)
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
            setHeader("Cache-Control", "public, max-age=604800, immutable")
            writer.use { it.write(data) }
        }
    }
}
