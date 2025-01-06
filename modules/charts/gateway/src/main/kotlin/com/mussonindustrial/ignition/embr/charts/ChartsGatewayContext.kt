package com.mussonindustrial.ignition.embr.charts

import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.mussonindustrial.embr.gateway.EmbrGatewayContext
import com.mussonindustrial.embr.gateway.EmbrGatewayContextImpl

class ChartsGatewayContext(private val context: GatewayContext) :
    EmbrGatewayContext by EmbrGatewayContextImpl(context) {
    companion object {
        lateinit var instance: ChartsGatewayContext
    }

    val perspectiveContext: PerspectiveContext

    init {
        instance = this
        perspectiveContext = PerspectiveContext.get(context)
    }
}
