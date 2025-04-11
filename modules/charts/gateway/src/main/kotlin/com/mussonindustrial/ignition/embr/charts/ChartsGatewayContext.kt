package com.mussonindustrial.ignition.embr.charts

import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.mussonindustrial.embr.gateway.EmbrGatewayContext
import com.mussonindustrial.embr.gateway.EmbrGatewayContextImpl
import com.mussonindustrial.embr.perspective.gateway.component.asGatewayComponent
import com.mussonindustrial.embr.perspective.gateway.component.registerComponent
import com.mussonindustrial.embr.perspective.gateway.component.removeComponent
import com.mussonindustrial.ignition.embr.charts.component.chart.ChartJs
import com.mussonindustrial.ignition.embr.charts.component.chart.ChartJsModelDelegate

class ChartsGatewayContext(private val context: GatewayContext) :
    EmbrGatewayContext by EmbrGatewayContextImpl(context) {
    companion object {
        lateinit var instance: ChartsGatewayContext
    }

    private val perspectiveContext: PerspectiveContext
    private val components = listOf(ChartJs.asGatewayComponent { ChartJsModelDelegate(it) })

    init {
        instance = this
        perspectiveContext = PerspectiveContext.get(context)
    }

    fun registerComponents() {
        components.forEach { perspectiveContext.registerComponent(it) }
    }

    fun removeComponents() {
        components.forEach { perspectiveContext.removeComponent(it) }
    }
}
