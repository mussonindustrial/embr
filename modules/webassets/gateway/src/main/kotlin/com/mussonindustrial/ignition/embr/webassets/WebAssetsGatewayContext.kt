package com.mussonindustrial.ignition.embr.webassets

import com.inductiveautomation.ignition.gateway.model.DiagnosticsManager
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.ignition.gateway.model.TelemetryManager
import com.mussonindustrial.embr.common.logging.getLogger
import com.mussonindustrial.embr.gateway.EmbrGatewayContext
import com.mussonindustrial.embr.gateway.EmbrGatewayContextImpl
import com.mussonindustrial.embr.servlets.ModuleServletManager
import com.mussonindustrial.ignition.embr.webassets.servlets.WebJarServlet
import com.mussonindustrial.ignition.embr.webassets.servlets.WebResourcesServlet
import com.mussonindustrial.ignition.embr.webassets.webjars.FolderWatcher
import com.mussonindustrial.ignition.embr.webassets.webjars.WebJarClassLoader

class WebAssetsGatewayContext(private val context: GatewayContext) :
    EmbrGatewayContext by EmbrGatewayContextImpl(context) {
    companion object {
        lateinit var instance: WebAssetsGatewayContext
    }

    private val dataPath = context.systemManager.dataDir.resolve("modules/${Meta.MODULE_ID}")
    private val webjarsFolder = dataPath.resolve("webjars")
    val resourcesFolder = dataPath.resolve("resources")

    private val servletManager = ModuleServletManager(context.webResourceManager, "/data/webassets")
    val webjarClassLoader = WebJarClassLoader(context, webjarsFolder)

    private val webjarFolderWatcher =
        FolderWatcher(webjarsFolder).apply { addListener(webjarClassLoader) }

    init {
        instance = this
        webjarsFolder.mkdirs()
        resourcesFolder.mkdirs()
        getLogger().info("${webjarsFolder.toURI().toURL()}")
    }

    fun registerModuleObservers() {
        context.moduleManager.addModuleObserver(webjarClassLoader)
    }

    fun removeModuleObservers() {
        context.moduleManager.removeModuleObserver(webjarClassLoader)
    }

    fun registerServlets() {
        WebResourcesServlet.register(servletManager)
        WebJarServlet.register(servletManager)
    }

    fun removeServlets() {
        WebResourcesServlet.unregister(servletManager)
        WebJarServlet.unregister(servletManager)
    }

    fun startWebJarFolderWatcher() {
        executorService.submit(webjarFolderWatcher)
    }

    fun stopWebJarFolderWatcher() {
        webjarFolderWatcher.stop()
    }

    override fun getTelemetryManager(): TelemetryManager? {
        return super.getTelemetryManager()
    }

    override fun getDiagnosticsManager(): DiagnosticsManager? {
        return super.getDiagnosticsManager()
    }
}
