package com.mussonindustrial.ignition.embr.common.gson

import com.inductiveautomation.ignition.common.gson.JsonElement

interface SimpleJsonSerializable {
    fun toGson(): JsonElement
}
