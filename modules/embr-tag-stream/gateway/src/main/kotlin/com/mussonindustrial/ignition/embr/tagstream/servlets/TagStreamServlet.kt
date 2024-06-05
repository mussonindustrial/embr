package com.mussonindustrial.ignition.embr.tagstream.servlets

import com.mussonindustrial.ignition.embr.tagstream.TagStreamGatewayHook
import com.mussonindustrial.ignition.embr.tagstream.getLogger
import org.eclipse.jetty.servlets.EventSourceServlet
import org.eclipse.jetty.servlets.EventSource
import javax.servlet.http.HttpServletRequest

class TagStreamServlet: EventSourceServlet() {

    private val logger = this.getLogger()
    private val tagStreamManager = TagStreamGatewayHook.context.tagStreamManager

    override fun newEventSource(request: HttpServletRequest): EventSource? {
        logger.debug("EventSource request received at URL: {}", request.requestURI)

        val id = request.requestURI.toString().split("/").last()
        logger.trace("TagStream ID parsed as {}.", id)

        val stream = tagStreamManager.getStream(id)
        stream?.let {
            logger.trace("Existing TagStream {} was found. Initializing session.", id)
            return TagEventSource(it)
        }

        logger.warn("Request received for an invalid TagStream ID.")
        return null
    }
}