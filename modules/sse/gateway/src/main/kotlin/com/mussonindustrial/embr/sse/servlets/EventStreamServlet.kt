package com.mussonindustrial.embr.sse.servlets

import com.mussonindustrial.embr.common.logging.getLogger
import com.mussonindustrial.embr.sse.EventStreamGatewayContext
import jakarta.servlet.http.HttpServletRequest
import org.eclipse.jetty.ee10.servlets.EventSource
import org.eclipse.jetty.ee10.servlets.EventSourceServlet

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
