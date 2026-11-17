package com.mussonindustrial.ignition.embr.periscope.handlers

import com.inductiveautomation.ignition.gateway.dataroutes.RequestContext
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import com.mussonindustrial.ignition.embr.periscope.js.modules.VirtualModule
import java.net.URLDecoder
import javax.servlet.http.HttpServletResponse

class VirtualModuleHandler(context: PeriscopeGatewayContext, modules: List<VirtualModule>) :
    SessionRouteHandler(context, "text/javascript") {

    val logger = this.getLoggerEx()

    val modulesByName = modules.associateBy { it.name }

    fun handleVirtualModule(response: HttpServletResponse, name: String) {
        val js = modulesByName[name]?.emit() ?: "/* Module not found: $name */"

        response.apply {
            contentType = "text/javascript"
            cacheForever()
            writer.use { it.write(js) }
        }
    }

    override fun respond(request: RequestContext, response: HttpServletResponse) {
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
