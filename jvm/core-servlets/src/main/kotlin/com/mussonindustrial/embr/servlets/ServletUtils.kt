package com.mussonindustrial.embr.servlets

import com.inductiveautomation.ignition.gateway.web.WebResourceManager
import com.mussonindustrial.embr.common.reflect.getPrivateProperty
import org.eclipse.jetty.server.Server

fun <T : WebResourceManager> T.getJettyServer(): Server {
    val ignitionServer = this.getPrivateProperty("server")
    return ignitionServer?.getPrivateProperty("server") as Server
}
