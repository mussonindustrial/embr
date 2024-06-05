package com.mussonindustrial.ignition.embr.tagstream.tags

import com.inductiveautomation.ignition.common.tags.model.TagManager

class TagStreamManager(private val tagManager: TagManager) {

    private val streams = hashMapOf<String, TagStream>()
    
    fun closeAllStreams() {
        streams.values.forEach {
            it.close()
        }
        streams.clear()
    }

    fun openStream(paths: List<String>): TagStream {
        val id = TagStream.getID(paths)
        val stream = getStream(id)
        stream?.let {
            return it
        }

        val tagStream = TagStream(tagManager, paths)
        streams[tagStream.id] = tagStream
        return tagStream
    }

    fun getStream(id: String): TagStream? {
        return streams[id]
    }
}



