package com.mussonindustrial.ignition.embr.eventstream.api

import com.mussonindustrial.ignition.embr.common.gson.JsonSerializable

interface AuthRequestCompanion<T : AuthRequest> {
    val type: String
    val gsonAdapter: JsonSerializable<T>
}
