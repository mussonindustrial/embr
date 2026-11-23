package com.mussonindustrial.embr.perspective.gateway.routes

import com.inductiveautomation.ignition.gateway.dataroutes.HttpMethod
import com.inductiveautomation.ignition.gateway.dataroutes.RequestContext
import com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup
import com.inductiveautomation.ignition.gateway.dataroutes.RouteHandler
import com.mussonindustrial.embr.perspective.gateway.reflect.PerspectiveRoutes
import jakarta.servlet.http.HttpServletResponse

abstract class SessionRouteHandler(private val contentType: String? = null) : RouteHandler {

    fun mount(mounter: RouteGroup.RouteMounter) {
        val route = mounter.method(HttpMethod.GET)
        if (contentType != null) {
            route.type(contentType)
        }

        route.accessControl(PerspectiveRoutes.requireSession()).handler(this).mount()
    }

    final override fun handle(request: RequestContext, response: HttpServletResponse): Any? {
        respond(request, response)
        return null
    }

    protected abstract fun respond(request: RequestContext, response: HttpServletResponse)
}
