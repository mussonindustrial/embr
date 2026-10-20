package com.mussonindustrial.embr.servlets

import com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup
import com.inductiveautomation.ignition.gateway.web.WebResourceManager
import com.mussonindustrial.embr.common.reflect.getPrivateProperty
import com.mussonindustrial.embr.common.reflect.getSuperPrivateMethod
import com.mussonindustrial.embr.common.reflect.getSuperPrivateProperty
import org.eclipse.jetty.server.Server

fun <T : WebResourceManager> T.getJettyServer(): Server {
    val ignitionServer = this.getPrivateProperty("server")
    return ignitionServer?.getPrivateProperty("server") as Server
}

fun <T : WebResourceManager> T.createDataServletRouteGroup(groupKey: String): RouteGroup {
    val dataServlet = this.getSuperPrivateProperty("dataServlet")
    val createRouteGroup = dataServlet.getSuperPrivateMethod("createRouteGroup", String::class.java)
    return createRouteGroup.invoke(dataServlet, groupKey) as RouteGroup
}

fun <T : WebResourceManager> T.removeDataServletRouteGroup(groupKey: String) {
    val dataServlet = this.getSuperPrivateProperty("dataServlet")
    val routeGroups = dataServlet.getSuperPrivateProperty("routeGroups") as MutableMap<*, *>
    routeGroups.remove(groupKey)
}
