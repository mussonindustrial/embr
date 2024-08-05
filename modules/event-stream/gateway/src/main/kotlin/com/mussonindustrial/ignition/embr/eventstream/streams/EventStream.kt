package com.mussonindustrial.ignition.embr.eventstream.streams

import com.inductiveautomation.ignition.common.gson.JsonElement
import com.mussonindustrial.ignition.embr.common.gson.SimpleJsonSerializable

interface EventStream : SessionStateListener, SimpleJsonSerializable {
    val key: String

    fun initialize(props: JsonElement)
}
