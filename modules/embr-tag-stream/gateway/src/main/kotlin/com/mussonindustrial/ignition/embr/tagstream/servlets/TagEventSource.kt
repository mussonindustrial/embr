package com.mussonindustrial.ignition.embr.tagstream.servlets

import com.inductiveautomation.ignition.common.tags.config.TagGson
import com.inductiveautomation.ignition.common.tags.model.event.TagChangeEvent
import com.mussonindustrial.ignition.embr.tagstream.getLogger
import com.mussonindustrial.ignition.embr.tagstream.tags.TagStream
import org.eclipse.jetty.servlets.EventSource

class TagEventSource(private val tagStream: TagStream): EventSource {

    private val logger = this.getLogger()
    private lateinit var emitter: EventSource.Emitter
    private val gson = TagGson.create()

    fun notifyTagChange(event: TagChangeEvent) {
        val type = event.tagPath.toString()
        logger.trace("Notifying session of tag change event for {}.", type)

        val data = gson.toJsonTree(event.value).toString()
        emitter.event(type, data)
    }

    override fun onOpen(emitter: EventSource.Emitter) {
        logger.debug("New event source opened.")
        this.emitter = emitter
        tagStream.addSession(this)
        emitter.data("session opened")

        // Send last known tag values.
        logger.trace("Sending last tag events.")
        tagStream.lastEvents.values.forEach { notifyTagChange(it) }
    }

    override fun onClose() {
        logger.debug("Event source closed.")
        tagStream.removeSession(this)
    }
}