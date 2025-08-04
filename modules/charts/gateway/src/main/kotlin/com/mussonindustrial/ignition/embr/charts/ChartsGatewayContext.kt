package com.mussonindustrial.ignition.embr.charts

import com.codahale.metrics.health.HealthCheckRegistry
import com.inductiveautomation.ignition.gateway.model.DiagnosticsManager
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.ignition.gateway.model.TelemetryManager
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.mussonindustrial.embr.gateway.EmbrGatewayContext
import com.mussonindustrial.embr.gateway.EmbrGatewayContextImpl
import com.mussonindustrial.embr.perspective.gateway.component.JavaScriptProxyableComponentModelDelegate
import com.mussonindustrial.embr.perspective.gateway.component.asGatewayComponent
import com.mussonindustrial.embr.perspective.gateway.component.registerComponent
import com.mussonindustrial.embr.perspective.gateway.component.removeComponent
import com.mussonindustrial.ignition.embr.charts.component.chart.ApexCharts
import com.mussonindustrial.ignition.embr.charts.component.chart.ApexChartsLegacy
import com.mussonindustrial.ignition.embr.charts.component.chart.ApexChartsLegacyModelDelegate
import com.mussonindustrial.ignition.embr.charts.component.chart.ChartJs
import com.mussonindustrial.ignition.embr.charts.modules.KyvisLabsApexCharts

class ChartsGatewayContext(private val context: GatewayContext) :
    EmbrGatewayContext by EmbrGatewayContextImpl(context) {
    companion object {
        lateinit var instance: ChartsGatewayContext
    }

    private val perspectiveContext: PerspectiveContext
    private val components =
        listOf(
            ApexCharts.asGatewayComponent { JavaScriptProxyableComponentModelDelegate(it) },
            ApexChartsLegacy.asGatewayComponent { ApexChartsLegacyModelDelegate(it) },
            ChartJs.asGatewayComponent { JavaScriptProxyableComponentModelDelegate(it) },
        )

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

    override fun getHealthCheckRegistry(): HealthCheckRegistry? {
        return super.getHealthCheckRegistry()
    }

    override fun getTelemetryManager(): TelemetryManager? {
        return super.getTelemetryManager()
    }

    override fun getDiagnosticsManager(): DiagnosticsManager? {
        return super.getDiagnosticsManager()
    }
}
