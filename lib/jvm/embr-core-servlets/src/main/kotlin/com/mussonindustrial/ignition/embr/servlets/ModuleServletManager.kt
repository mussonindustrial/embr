package com.mussonindustrial.ignition.embr.servlets

import com.inductiveautomation.ignition.gateway.web.WebResourceManager
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import javax.servlet.Servlet

class ModuleServletManager(webResourceManager: WebResourceManager, private val contextPath: String) {

    private val servlets = hashMapOf<String, Class<out Servlet>>()
    private val runningServlets = hashMapOf<String, ServletHolder>()
    private var handler: ServletContextHandler? = null
    private val serverHandlerCollection = webResourceManager.getJettyServer().handler as HandlerCollection

    private fun getContextHandler(): ServletContextHandler {
        return ServletContextHandler().apply {
            contextPath = this@ModuleServletManager.contextPath
            servlets.forEach {
                runningServlets[it.key] = this.addServlet(it.value, it.key)
            }
        }
    }

    fun addServlet(path: String, servlet: Class<out Servlet>) {
        servlets[path] = servlet
        val newHandler = getContextHandler()
        serverHandlerCollection.removeHandler(handler)
        serverHandlerCollection.prependHandler(newHandler)
        newHandler.start()
        handler = newHandler
    }

    fun removeAllServlets() {
        runningServlets.forEach {(path, servlet) ->
            servlet.stop()
            servlets.remove(path)
            serverHandlerCollection.removeHandler(handler)
        }
        runningServlets.clear()
    }

    fun removeServlet(path: String) {
        runningServlets[path]?.let{
            it.stop()
            servlets.remove(path)
            runningServlets.remove(path)

            val newHandler = getContextHandler()
            serverHandlerCollection.removeHandler(handler)
            serverHandlerCollection.prependHandler(newHandler)
            newHandler.start()
            handler = newHandler
        }

    }
}