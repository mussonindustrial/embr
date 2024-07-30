package com.mussonindustrial.ignition.embr.eventstream.streams

interface EventStreamCompanion<T : EventStream> {
    val key: String

    fun get(): T
}
