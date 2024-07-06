package com.mussonindustrial.ignition.embr.tagstream.emitters

import com.mussonindustrial.ignition.embr.tagstream.EventStreamManager

interface SessionStateListener {

    fun onCreation(session: EventStreamManager.Session)

    fun onOpen()

    fun onClose()

}