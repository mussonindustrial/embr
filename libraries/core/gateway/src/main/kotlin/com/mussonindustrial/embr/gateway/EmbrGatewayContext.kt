package com.mussonindustrial.embr.gateway

import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.mussonindustrial.embr.common.EmbrCommonContextExtension

interface EmbrGatewayContext :
    GatewayContext, EmbrCommonContextExtension, EmbrGatewayContextExtension {

    fun getModuleSafe(moduleId: String): Any? {
        return try {
            moduleManager.getModule(moduleId)
        } catch (_: Throwable) {
            null
        }
    }

    fun <T> ifModule(moduleId: String, action: () -> T): T? {
        if (getModuleSafe(moduleId) != null) {
            return action()
        }
        return null
    }
}
