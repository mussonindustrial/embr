package com.mussonindustrial.ignition.embr.tagstream.emitters

import com.inductiveautomation.ignition.common.gson.JsonElement

interface EventEmitter: SessionStateListener {

    val key: String
    fun initialize(props: JsonElement)

}