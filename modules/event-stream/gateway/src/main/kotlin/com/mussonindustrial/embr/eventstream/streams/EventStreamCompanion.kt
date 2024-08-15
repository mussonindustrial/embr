package com.mussonindustrial.embr.eventstream.streams

interface EventStreamCompanion<T : EventStream> {
    val key: String

    fun get(): T
}
