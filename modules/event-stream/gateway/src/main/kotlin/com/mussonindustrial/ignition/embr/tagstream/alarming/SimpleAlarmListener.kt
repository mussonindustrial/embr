package com.mussonindustrial.ignition.embr.tagstream.alarming

import com.inductiveautomation.ignition.common.alarming.AlarmEvent
import com.inductiveautomation.ignition.common.alarming.AlarmListener

fun interface SimpleAlarmListener: AlarmListener {

    fun onAlarmEvent(event: AlarmEvent)

    override fun onActive(event: AlarmEvent) {
        onAlarmEvent(event)
    }

    override fun onClear(event: AlarmEvent) {
        onAlarmEvent(event)
    }

    override fun onAcknowledge(event: AlarmEvent) {
        onAlarmEvent(event)
    }
}