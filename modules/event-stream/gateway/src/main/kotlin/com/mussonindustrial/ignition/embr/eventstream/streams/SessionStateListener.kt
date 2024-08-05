package com.mussonindustrial.ignition.embr.eventstream.streams

interface SessionStateListener {
    fun onCreate(session: EventStreamManager.Session)

    fun onOpen()

    fun onClose()
}
