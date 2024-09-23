package com.mussonindustrial.embr.thermo

import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.mussonindustrial.embr.gateway.EmbrGatewayContext
import com.mussonindustrial.embr.gateway.EmbrGatewayContextImpl

data class ThermoGatewayContext(val context: GatewayContext) :
    EmbrGatewayContext by EmbrGatewayContextImpl(context) {
    companion object {
        lateinit var instance: ThermoGatewayContext
    }

    init {
        instance = this
    }
}
