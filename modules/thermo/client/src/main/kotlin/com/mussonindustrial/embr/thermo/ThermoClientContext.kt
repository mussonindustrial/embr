package com.mussonindustrial.embr.thermo

import com.inductiveautomation.ignition.client.model.ClientContext
import com.mussonindustrial.embr.client.EmbrClientContext
import com.mussonindustrial.embr.client.EmbrClientContextImpl

data class ThermoClientContext(val context: ClientContext) :
    EmbrClientContext by EmbrClientContextImpl(context) {
    companion object {
        lateinit var instance: ThermoClientContext
    }

    init {
        instance = this
    }
}
