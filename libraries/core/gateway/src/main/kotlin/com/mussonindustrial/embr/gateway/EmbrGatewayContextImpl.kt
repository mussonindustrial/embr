package com.mussonindustrial.embr.gateway

import com.inductiveautomation.ignition.gateway.model.DiagnosticsManager
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.ignition.gateway.model.TelemetryManager
import com.mussonindustrial.embr.common.EmbrCommonContextExtension
import com.mussonindustrial.embr.common.EmbrCommonContextExtensionImpl
import com.mussonindustrial.embr.common.EmbrModuleMeta

open class EmbrGatewayContextImpl(private val context: GatewayContext) :
    EmbrGatewayContext,
    GatewayContext by context,
    EmbrCommonContextExtension by EmbrCommonContextExtensionImpl(context) {

    override fun isModuleInstalled(module: EmbrModuleMeta): Boolean {
        return context.moduleManager.modules.find { it.info.id == module.id } != null
    }

    override fun getTelemetryManager(): TelemetryManager {
        return context.telemetryManager
    }

    override fun getDiagnosticsManager(): DiagnosticsManager {
        return context.diagnosticsManager
    }
}
