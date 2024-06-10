package com.mussonindustrial.ignition.embr.tagstream.tags

import com.inductiveautomation.ignition.common.tags.model.TagManager
import com.inductiveautomation.ignition.common.tags.model.event.TagChangeEvent
import com.inductiveautomation.ignition.common.tags.model.event.TagChangeListener
import com.inductiveautomation.ignition.common.tags.paths.parser.TagPathParser
import com.mussonindustrial.ignition.embr.tagstream.getLogger
import com.mussonindustrial.ignition.embr.tagstream.servlets.TagEventSource
import java.util.*

class TagStream(private val tagManager: TagManager, paths: List<String>): TagChangeListener {
    private val logger = this.getLogger()
    companion object {
        fun getID(paths: List<String>): String {
            val sortedPaths = paths.sortedDescending()
            return UUID.nameUUIDFromBytes(sortedPaths.toString().toByteArray()).toString()
        }
    }

    val id by lazy { getID(paths) }
    private val tagPaths = paths.map { TagPathParser.parse(it) }
    private val listeners = tagPaths.map { this }
    private val sessions = mutableSetOf<TagEventSource>()
    val lastEvents = mutableMapOf<String, TagChangeEvent>()

    init {
        tagManager.subscribeAsync(tagPaths, listeners)
    }

    fun close() {
        logger.debug("Closing TagStream {}.", id)
        tagManager.unsubscribeAsync(tagPaths, listeners)
        sessions.clear()
        lastEvents.clear()
    }

    fun addSession(session: TagEventSource) {
        logger.debug("Adding session to TagStream {}.", id)
        sessions.add(session)
    }

    fun removeSession(session: TagEventSource) {
        logger.debug("Removing session from TagStream {}.", id)
        sessions.remove(session)
    }

    override fun tagChanged(event: TagChangeEvent) {
        logger.trace("tagChanged: {}", event)
        lastEvents[event.tagPath.toString()] = event

        val failedSessions = mutableSetOf<TagEventSource>()
        sessions.forEach{
            try {
                it.notifyTagChange(event)
            } catch (e: Throwable) {
                logger.warn("Error during TagStream {}. Dropping bad session.", id)
                failedSessions.add(it)
            }
        }

        failedSessions.forEach {
            it.onClose()
        }
    }

}