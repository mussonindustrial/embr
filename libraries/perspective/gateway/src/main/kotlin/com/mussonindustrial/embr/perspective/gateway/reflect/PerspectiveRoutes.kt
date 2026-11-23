package com.mussonindustrial.embr.perspective.gateway.reflect

import com.inductiveautomation.ignition.gateway.dataroutes.AccessControlStrategy
import com.inductiveautomation.perspective.gateway.api.SessionScope
import com.inductiveautomation.perspective.gateway.comm.Routes
import com.inductiveautomation.perspective.gateway.session.PerspectiveSessionCollection
import com.mussonindustrial.embr.common.reflect.getPrivateMethod
import jakarta.servlet.http.HttpServletRequest
import java.util.EnumSet
import kotlin.jvm.optionals.getOrNull

object PerspectiveRoutes {

    private val requireSession =
        Routes::class.java.getPrivateMethod("requireSession", EnumSet::class.java)

    fun requireSession(
        scopes: EnumSet<SessionScope> = EnumSet.allOf(SessionScope::class.java),
    ): AccessControlStrategy {
        return requireSession.invoke(null, scopes) as AccessControlStrategy
    }

    fun getSession(request: HttpServletRequest): PerspectiveSessionCollection? {
        return Routes.getSession(request).getOrNull()
    }
}
