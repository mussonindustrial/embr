package com.mussonindustrial.embr.sse.streams

interface EventStreamCompanion<T : EventStream> {
    val key: String

    fun get(): T
}
