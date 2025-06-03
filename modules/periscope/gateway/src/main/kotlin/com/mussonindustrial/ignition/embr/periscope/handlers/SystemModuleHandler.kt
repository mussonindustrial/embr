package com.mussonindustrial.ignition.embr.periscope.handlers

import com.inductiveautomation.ignition.gateway.dataroutes.HttpMethod
import com.inductiveautomation.ignition.gateway.dataroutes.RequestContext
import com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup
import com.inductiveautomation.ignition.gateway.dataroutes.RouteHandler
import com.inductiveautomation.perspective.gateway.api.SessionScope
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import com.mussonindustrial.ignition.embr.periscope.js.modules.SystemModule
import java.util.EnumSet
import javax.servlet.http.HttpServletResponse

class SystemModuleHandler(val context: PeriscopeGatewayContext) : RouteHandler {

    val logger = this.getLoggerEx()

    fun mount(mounter: RouteGroup.RouteMounter) {
        mounter
            .method(HttpMethod.GET)
            .type("text/javascript")
            .restrict(context.requireSession(EnumSet.allOf(SessionScope::class.java)))
            .handler(this)
            .mount()
    }

    val modules =
        listOf(
            SystemModule("periscope", listOf("resource", "toast")),
            SystemModule("perspective", listOf("context", "sendMessage")),
        )
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
        logger.trace("System Module request: $request")

        val moduleName =
            request.getParameter("module_name")
                ?: return response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing module name",
                )

        if (moduleName in modulesByName) {
            handleVirtualModule(response, moduleName)
            return
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND, "System Module not found: $moduleName")
    }
}
