package com.mussonindustrial.embr.sse.api

import com.mussonindustrial.embr.common.gson.JsonSerializable

interface AuthRequestCompanion<T : AuthRequest> {
    val type: String
    val gsonAdapter: JsonSerializable<T>
}
