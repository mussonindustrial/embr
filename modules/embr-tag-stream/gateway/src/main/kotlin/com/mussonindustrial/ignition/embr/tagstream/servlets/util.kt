package com.mussonindustrial.ignition.embr.tagstream.servlets

import com.inductiveautomation.ignition.gateway.web.WebResourceManager
import com.mussonindustrial.ignition.embr.tagstream.getPrivateProperty
import org.eclipse.jetty.server.Server


fun <T : WebResourceManager> T.getJettyServer(): Server {
    val ignitionServer = this.getPrivateProperty("server")
    return ignitionServer?.getPrivateProperty("server") as Server
}