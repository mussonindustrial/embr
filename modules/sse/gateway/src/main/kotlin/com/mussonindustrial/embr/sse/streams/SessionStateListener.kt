package com.mussonindustrial.embr.sse.streams

interface SessionStateListener {
    fun onCreate(session: EventStreamManager.Session)

    fun onOpen()

    fun onClose()
}
