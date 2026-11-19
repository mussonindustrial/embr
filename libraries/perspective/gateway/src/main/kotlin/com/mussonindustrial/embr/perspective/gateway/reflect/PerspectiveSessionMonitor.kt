package com.mussonindustrial.embr.perspective.gateway.reflect

import com.inductiveautomation.perspective.gateway.session.InternalSession
import com.inductiveautomation.perspective.gateway.session.PerspectiveSessionMonitor
import com.mussonindustrial.embr.common.reflect.getPrivateProperty

val PerspectiveSessionMonitor.runningSessions: List<InternalSession>
    get() {
        val sessions = this.getPrivateProperty("sessionsById") as Map<*, *>
        return sessions.values.filterIsInstance<InternalSession>()
    }
