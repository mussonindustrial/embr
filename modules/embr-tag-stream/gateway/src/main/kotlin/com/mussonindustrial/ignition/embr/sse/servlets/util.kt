package com.mussonindustrial.ignition.embr.sse.servlets

import com.inductiveautomation.ignition.gateway.web.WebResourceManager
import com.mussonindustrial.ignition.embr.sse.Meta.SHORT_MODULE_ID
import com.mussonindustrial.ignition.embr.sse.getPrivateProperty
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.servlet.ServletContextHandler
import javax.servlet.Servlet


val unmappedServlets = hashMapOf<String, ServletContextHandler>()

fun <T : WebResourceManager> T.getJettyServer(): Server {
    val ignitionServer = this.getPrivateProperty("server")
    return ignitionServer?.getPrivateProperty("server") as Server
}
fun <T : WebResourceManager> T.addUnmappedServlet(path: String, servlet: Class<out Servlet>) {
    val servletHandler = ServletContextHandler().apply {
        contextPath = "/$SHORT_MODULE_ID"
        addServlet(servlet, path)
    }
    unmappedServlets[path] = servletHandler
    (this.getJettyServer().handler as HandlerCollection).prependHandler(servletHandler)
    servletHandler.start()
}

fun <T : WebResourceManager> T.removeUnmappedServlet(path: String) {
    unmappedServlets[path]?.let{
        it.stop()
        (this.getJettyServer().handler as HandlerCollection).removeHandler(it)
    }
}