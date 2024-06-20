package com.mussonindustrial.ignition.embr.tagstream

import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.ignition.gateway.web.WebResourceManager
import com.mussonindustrial.ignition.embr.tagstream.servlets.ModuleServletManager
import org.eclipse.jetty.server.Server

data class TagStreamGatewayContext(val context: GatewayContext): GatewayContext by context {
    val tagStreamMetricsProvider = TagStreamMetricsProvider(context.tagManager)
    val tagStreamManager = TagStreamManager(this)
    val servletManager = ModuleServletManager(context.webResourceManager, Meta.URL_ALIAS)
}

fun <T : WebResourceManager> T.getJettyServer(): Server {
    val ignitionServer = this.getPrivateProperty("server")
    return ignitionServer?.getPrivateProperty("server") as Server
}