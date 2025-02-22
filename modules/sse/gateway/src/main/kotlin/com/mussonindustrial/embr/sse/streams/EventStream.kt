package com.mussonindustrial.embr.sse.streams

import com.inductiveautomation.ignition.common.gson.JsonElement
import com.mussonindustrial.embr.common.gson.SimpleJsonSerializable

interface EventStream : SessionStateListener, SimpleJsonSerializable {
    val key: String

    fun initialize(props: JsonElement)
}
