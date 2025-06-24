package com.mussonindustrial.ignition.embr.webassets

import com.inductiveautomation.ignition.gateway.model.DiagnosticsManager
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.ignition.gateway.model.TelemetryManager
import com.mussonindustrial.embr.gateway.EmbrGatewayContext
import com.mussonindustrial.embr.gateway.EmbrGatewayContextImpl
import com.mussonindustrial.embr.servlets.ModuleServletManager
import com.mussonindustrial.ignition.embr.webassets.reflect.GatewayModulesClassLoader
import com.mussonindustrial.ignition.embr.webassets.servlets.WebJarServlet
import com.mussonindustrial.ignition.embr.webassets.servlets.WebResourcesServlet

class WebAssetsGatewayContext(private val context: GatewayContext) :
    EmbrGatewayContext by EmbrGatewayContextImpl(context) {
    companion object {
        lateinit var instance: WebAssetsGatewayContext
    }

    val servletManager = ModuleServletManager(context.webResourceManager, "/data/webassets")
    val gatewayModulesClassLoader = GatewayModulesClassLoader(context)

    init {
        instance = this
    }

    fun registerModuleObservers() {
        context.moduleManager.addModuleObserver(gatewayModulesClassLoader)
    }

    fun removeModuleObservers() {
        context.moduleManager.removeModuleObserver(gatewayModulesClassLoader)
    }

    fun registerServlets() {
        WebResourcesServlet.register(servletManager)
        WebJarServlet.register(servletManager)
    }

    fun removeServlets() {
        WebResourcesServlet.unregister(servletManager)
        WebJarServlet.unregister(servletManager)
    }

    override fun getTelemetryManager(): TelemetryManager? {
        return super.getTelemetryManager()
    }

    override fun getDiagnosticsManager(): DiagnosticsManager? {
        return super.getDiagnosticsManager()
    }
}
