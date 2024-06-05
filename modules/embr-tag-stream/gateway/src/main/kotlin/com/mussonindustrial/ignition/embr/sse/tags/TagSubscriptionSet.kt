package com.mussonindustrial.ignition.embr.sse.tags

import com.inductiveautomation.ignition.common.tags.model.TagManager
import com.inductiveautomation.ignition.common.tags.model.TagPath
import com.inductiveautomation.ignition.common.tags.model.event.TagChangeEvent
import com.inductiveautomation.ignition.common.tags.model.event.TagChangeListener
import com.inductiveautomation.ignition.common.tags.paths.parser.TagPathParser
import org.eclipse.jetty.servlets.EventSource
import org.slf4j.LoggerFactory
import java.util.*

class TagStreamManager(private val tagManager: TagManager) {

    private val streams = hashMapOf<String, TagStream>()

    fun createStream(paths: List<String>): TagStream {
        val subscriptionSet = TagStream(tagManager, paths)
        streams[subscriptionSet.id] = subscriptionSet
        return subscriptionSet
    }

    fun getOrCreateStream(paths: List<String>): TagStream {
        val id = TagStream.getID(paths)
        val stream = getStream(id)
        stream?.let {
            return it
        }
        return createStream(paths)
    }

    fun getStream(id: String): TagStream? {
        return streams[id]
    }
}




class TagStream(private val tagManager: TagManager, paths: List<String>) {
    private val logger = LoggerFactory.getLogger("TagStream")
    companion object {
        fun getID(paths: List<String>): String {
            return UUID.nameUUIDFromBytes(paths.toString().toByteArray()).toString()
        }
    }

    private val listener = TagStreamChangeListener()

    val id = getID(paths)
    private val tagPaths = paths.map { TagPathParser.parse(it) }
    private val listeners = tagPaths.map { listener }
    private val emitters = mutableListOf<EventSource.Emitter>()

    fun addEmitter(emitter: EventSource.Emitter) {
        if (emitters.isEmpty()) {
            tagManager.subscribeAsync(tagPaths, listeners).whenComplete { _, t ->
                if (t == null)
                    logger.info("Subscribe Success")
                else
                    logger.warn("Subscribe Fail", t)
            }
        }
        logger.info("Adding emitter to stream $id")
        emitters.add(emitter)
    }

    fun removeEmitter(emitter: EventSource.Emitter) {
        logger.info("Removing emitter from stream $id")
        emitters.remove(emitter)
        if (emitters.isEmpty()) {
            tagManager.unsubscribeAsync(tagPaths, listeners)
        }
    }

    inner class TagStreamChangeListener: TagChangeListener {
        override fun tagChanged(event: TagChangeEvent?) {
            emitters.forEach{
                try {
                    it.event(event?.tagPath.toString(), event?.value?.value.toString())
                } catch (e: Throwable) {
                    logger.error("Error notifying tag changed. Unregistering emitter.")
                    emitters.remove(it)
                }

            }
        }
    }



}