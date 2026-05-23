package com.mussonindustrial.embr.perspective.gateway.session

import com.google.common.eventbus.Subscribe
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.inductiveautomation.perspective.gateway.event.SessionShutdownEvent
import com.inductiveautomation.perspective.gateway.event.SessionStartupEvent
import com.inductiveautomation.perspective.gateway.session.InternalSession
import java.util.Objects
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

class PerspectiveSessionMonitor(context: PerspectiveContext) {
    val sessionsByProject: MutableMap<SessionKey?, InternalSession?> = ConcurrentHashMap()
    val sessionsById: MutableMap<UUID?, InternalSession?> = ConcurrentHashMap()

    init {
        context.eventBus.register(this)
    }

    @Subscribe
    fun onSessionStarted(event: SessionStartupEvent) {
        val session = event.session
        this.sessionsByProject.put(SessionKey(session), session)
        this.sessionsById.put(session.sessionId, session)
    }

    @Subscribe
    fun onSessionShutdown(event: SessionShutdownEvent) {
        val session = event.session
        this.sessionsByProject.remove(SessionKey(session))
        this.sessionsById.remove(session.sessionId)
    }

    fun getSessionsForProject(projectName: String): MutableList<InternalSession> {
        return this.sessionsByProject.values
            .stream()
            .filter { session: InternalSession? -> session != null }
            .filter { session: InternalSession? ->
                session!!.isRunning && session.projectName == projectName
            }
            .collect(Collectors.toList())
    }

    class SessionKey
    internal constructor(private val authId: String, private val projectName: String) {
        internal constructor(
            session: InternalSession
        ) : this(session.sessionCollection.uuid, session.projectName)

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            } else if (other != null && this.javaClass == other.javaClass) {
                val that = other as SessionKey
                return this.authId == that.authId && this.projectName == that.projectName
            } else {
                return false
            }
        }

        override fun hashCode(): Int {
            return Objects.hash(*arrayOf<Any?>(this.authId, this.projectName))
        }
    }
}
