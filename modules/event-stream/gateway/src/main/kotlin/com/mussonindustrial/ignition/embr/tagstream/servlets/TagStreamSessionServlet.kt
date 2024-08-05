package com.mussonindustrial.ignition.embr.tagstream.servlets

import com.mussonindustrial.ignition.embr.common.logging.getLogger
import com.mussonindustrial.ignition.embr.tagstream.TagStreamGatewayHook
import org.eclipse.jetty.servlets.EventSourceServlet
import org.eclipse.jetty.servlets.EventSource
import javax.servlet.http.HttpServletRequest

class TagStreamSessionServlet: EventSourceServlet() {
    private val logger = this.getLogger()
    private val tagStreamManager = TagStreamGatewayHook.context.tagStreamManager

    override fun newEventSource(request: HttpServletRequest): EventSource? {
        logger.trace("TagStreamServlet request received at URL: {}", request.requestURI)

        val path = request.requestURI.substring(request.contextPath.length)
        val id = path.split("/").last()
        logger.trace("Session ID parsed as {}.", id)

        val session = tagStreamManager.joinSession(id)
        session?.let {
            logger.trace("Session {} was found. Joining session.", id)
            return it
        }

        logger.warn("Request received for an invalid Session ID.")
        return null
    }
}