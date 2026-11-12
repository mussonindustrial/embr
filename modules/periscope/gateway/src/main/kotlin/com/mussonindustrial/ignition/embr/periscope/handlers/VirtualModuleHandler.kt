package com.mussonindustrial.ignition.embr.periscope.handlers

import com.inductiveautomation.ignition.gateway.dataroutes.HttpMethod
import com.inductiveautomation.ignition.gateway.dataroutes.RequestContext
import com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup
import com.inductiveautomation.ignition.gateway.dataroutes.RouteHandler
import com.inductiveautomation.perspective.gateway.api.SessionScope
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import com.mussonindustrial.ignition.embr.periscope.js.modules.VirtualModule
import jakarta.servlet.http.HttpServletResponse
import java.net.URLDecoder
import java.util.EnumSet

class VirtualModuleHandler(
    val context: PeriscopeGatewayContext,
    val modules: List<VirtualModule>,
) : RouteHandler {

    val logger = this.getLoggerEx()

    fun mount(mounter: RouteGroup.RouteMounter) {
        mounter
            .method(HttpMethod.GET)
            .type("text/javascript")
            .accessControl(context.requireSession(EnumSet.allOf(SessionScope::class.java)))
            .handler(this)
            .mount()
    }

    val modulesByName = modules.associateBy { it.name }

    fun handleVirtualModule(response: HttpServletResponse, name: String) {
        val js = modulesByName[name]?.emit() ?: "/* Module not found: $name */"

        response.apply {
            contentType = "text/javascript"
            setHeader("Cache-Control", "public, max-age=604800, immutable")
            writer.use { it.write(js) }
        }
    }

    override fun handle(request: RequestContext, response: HttpServletResponse) {
        logger.trace("Virtual module request: $request")

        val hash =
            request.getParameter("hash")
                ?: return response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing hash")

        val moduleName = URLDecoder.decode(request.path.substringAfter("$hash/"), Charsets.UTF_8)

        if (moduleName in modulesByName) {
            handleVirtualModule(response, moduleName)
            return
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Module not found: $moduleName")
    }
}
