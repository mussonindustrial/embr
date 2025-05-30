package com.mussonindustrial.embr.snmp

import com.inductiveautomation.ignition.client.model.ClientContext
import com.mussonindustrial.embr.client.EmbrClientContext
import com.mussonindustrial.embr.client.EmbrClientContextImpl

data class SnmpClientContext(val context: ClientContext) :
    EmbrClientContext by EmbrClientContextImpl(context) {
    companion object {
        lateinit var instance: SnmpClientContext
    }

    init {
        instance = this
    }
}
