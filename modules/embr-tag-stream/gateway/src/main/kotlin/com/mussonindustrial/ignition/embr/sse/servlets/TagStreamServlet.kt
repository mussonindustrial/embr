package com.mussonindustrial.ignition.embr.sse.servlets

import com.mussonindustrial.ignition.embr.sse.GatewayHook
import com.mussonindustrial.ignition.embr.sse.tags.TagStream
import org.eclipse.jetty.servlets.EventSourceServlet
import org.eclipse.jetty.servlets.EventSource
import org.slf4j.LoggerFactory
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletRequest

class TagStreamServlet: EventSourceServlet() {

    private val logger = LoggerFactory.getLogger("TagStreamServlet")
    private val context = GatewayHook.context
    private val tagStreamManager = GatewayHook.tagStreamManager


    override fun newEventSource(request: HttpServletRequest): EventSource? {
        val id = request.requestURI.toString().split("/").last()
        logger.info("newEventSource: id = $id")

        val stream = tagStreamManager.getStream(id)
        stream?.let {
            return TagEventSource(it)
        }
        return null
    }

    inner class TagEventSource(val tagStream: TagStream): EventSource {

        private lateinit var emitter: EventSource.Emitter
        private var update: Future<*>? = null
        private var count = 0
        private var closed = false
        override fun onOpen(emitter: EventSource.Emitter) {
            logger.info("New event source opened.")
            this.emitter = emitter
            tagStream.addEmitter(emitter)
//            this.scheduleUpdate()

            emitter.event("test", "test event data goes here")
            emitter.data("welcome to a TagEventSource")
        }

        private val doUpdate = Runnable {
            this.emitter.data(count.toString())
            count++
            scheduleUpdate()
        }

        private fun scheduleUpdate() {
            if (!this.closed) {
                update = context.scheduledExecutorService.schedule(
                    doUpdate,
                    10, TimeUnit.MILLISECONDS
                )
            }
        }

        override fun onClose() {
            logger.info("Event source closed.")
            this.closed = true
            this.update?.cancel(true)
            tagStream.removeEmitter(emitter)
        }
    }
}