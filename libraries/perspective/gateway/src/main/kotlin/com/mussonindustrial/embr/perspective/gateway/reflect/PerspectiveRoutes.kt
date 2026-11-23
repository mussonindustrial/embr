package com.mussonindustrial.embr.perspective.gateway.reflect

import com.inductiveautomation.ignition.gateway.dataroutes.RouteAccessControl
import com.inductiveautomation.perspective.gateway.api.SessionScope
import com.inductiveautomation.perspective.gateway.comm.Routes
import com.inductiveautomation.perspective.gateway.session.PerspectiveSessionCollection
import com.mussonindustrial.embr.common.reflect.getPrivateMethod
import java.util.EnumSet
import java.util.Optional
import javax.servlet.http.HttpServletRequest
import kotlin.jvm.optionals.getOrNull

object PerspectiveRoutes {

    private val requireSession =
        Routes::class.java.getPrivateMethod("requireSession", EnumSet::class.java)

    private val getSession =
        Routes::class.java.getPrivateMethod("getSession", HttpServletRequest::class.java)

    fun requireSession(
        scopes: EnumSet<SessionScope> = EnumSet.allOf(SessionScope::class.java),
    ): RouteAccessControl {
        return requireSession.invoke(null, scopes) as RouteAccessControl
    }

    fun getSession(request: HttpServletRequest): PerspectiveSessionCollection? {
        val session = getSession.invoke(null, request) as Optional<*>
        return session.getOrNull() as? PerspectiveSessionCollection
    }
}
