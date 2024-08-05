package com.mussonindustrial.ignition.embr.eventstream.servlets

import com.mussonindustrial.ignition.embr.common.logging.getLogger
import com.mussonindustrial.ignition.embr.eventstream.EventStreamGatewayContext
import org.eclipse.jetty.servlets.EventSource
import org.eclipse.jetty.servlets.EventSourceServlet
import javax.servlet.http.HttpServletRequest

class EventStreamServlet : EventSourceServlet() {
    private val logger = this.getLogger()
    private val context = EventStreamGatewayContext.instance
    private val eventStreamManager = context.eventStreamManager

    override fun newEventSource(request: HttpServletRequest): EventSource? {
        logger.trace("Request received at URL: {}", request.requestURI)

        val path = request.requestURI.substring(request.contextPath.length)
        val id = path.split("/").last()
        logger.trace("Session ID parsed as {}.", id)

        val session = eventStreamManager.getUnopenedSession(id)
        if (session == null) {
            logger.warn("Request received for an invalid Session ID.")
            return null
        }

        logger.trace("Session {} was found. Joining session.", id)
        return session
    }
}
