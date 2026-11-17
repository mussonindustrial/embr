package com.mussonindustrial.ignition.embr.periscope.handlers

import com.inductiveautomation.ignition.gateway.dataroutes.HttpMethod
import com.inductiveautomation.ignition.gateway.dataroutes.RequestContext
import com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup
import com.inductiveautomation.ignition.gateway.dataroutes.RouteHandler
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import jakarta.servlet.http.HttpServletResponse

abstract class SessionRouteHandler(
    protected val context: PeriscopeGatewayContext,
    private val contentType: String? = null,
) : RouteHandler {

    fun mount(mounter: RouteGroup.RouteMounter) {
        val route = mounter.method(HttpMethod.GET)
        if (contentType != null) {
            route.type(contentType)
        }

        route.accessControl(context.anySession).handler(this).mount()
    }

    final override fun handle(request: RequestContext, response: HttpServletResponse): Any? {
        respond(request, response)
        return null
    }

    protected abstract fun respond(request: RequestContext, response: HttpServletResponse)

    protected fun HttpServletResponse.cacheForever() {
        setHeader("Cache-Control", "public, max-age=31536000, immutable")
    }

    protected fun HttpServletResponse.cacheNever() {
        setHeader("Cache-Control", "no-store")
    }

    protected fun HttpServletResponse.revalidate() {
        setHeader("Cache-Control", "no-cache")
    }
}
