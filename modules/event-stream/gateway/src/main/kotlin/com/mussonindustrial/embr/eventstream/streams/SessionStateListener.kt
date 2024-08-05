package com.mussonindustrial.embr.eventstream.streams

interface SessionStateListener {
    fun onCreate(session: EventStreamManager.Session)

    fun onOpen()

    fun onClose()
}
