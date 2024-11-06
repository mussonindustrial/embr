package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.mussonindustrial.embr.gateway.EmbrGatewayContext
import com.mussonindustrial.embr.gateway.EmbrGatewayContextImpl

class PeriscopeGatewayContext(private val context: GatewayContext) :
    EmbrGatewayContext by EmbrGatewayContextImpl(context) {
    companion object {
        lateinit var instance: PeriscopeGatewayContext
    }

    val perspectiveContext: PerspectiveContext

    init {
        instance = this
        perspectiveContext = PerspectiveContext.get(context)
    }
}
