package com.mussonindustrial.ignition.embr.charts

import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.mussonindustrial.ignition.embr.gateway.EmbrGatewayContext
import com.mussonindustrial.ignition.embr.gateway.EmbrGatewayContextImpl

class ChartsGatewayContext(context: GatewayContext):
    EmbrGatewayContext by EmbrGatewayContextImpl(context) {
    companion object {
        lateinit var INSTANCE: ChartsGatewayContext
    }
    init {
        INSTANCE = this
    }

}