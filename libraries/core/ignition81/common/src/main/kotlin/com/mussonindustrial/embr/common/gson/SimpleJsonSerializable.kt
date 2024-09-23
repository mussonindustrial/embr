package com.mussonindustrial.embr.common.gson

import com.inductiveautomation.ignition.common.gson.JsonElement

interface SimpleJsonSerializable {
    fun toGson(): JsonElement
}
