package com.mussonindustrial.ignition.embr.webassets.webjars

import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.ignition.gateway.model.GatewayModule
import com.inductiveautomation.ignition.gateway.model.ModuleObserver
import com.mussonindustrial.embr.common.logging.getLogger
import com.mussonindustrial.embr.common.reflect.DelegatedClassLoader
import com.mussonindustrial.ignition.embr.webassets.reflect.GatewayModulesClassLoader
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class WebJarClassLoader(context: GatewayContext, private val folder: File) :
    ClassLoader(WebJarClassLoader::class.java.classLoader), FolderWatcher.Listener, ModuleObserver {

    private val logger = this.getLogger()
    private val gatewayModulesClassLoader = GatewayModulesClassLoader(context.moduleManager)
    private val classLoader = AtomicReference(createClassLoader())

    private fun File.listJars(): Array<URL> {
        return this.listFiles { dir, name -> dir.resolve(name).isFile && name.endsWith(".jar") }!!
            .map { it.toURI().toURL() }
            .toTypedArray()
    }

    private fun createClassLoader(): DelegatedClassLoader {

        val folderClassLoader = URLClassLoader(folder.listJars(), this::class.java.classLoader)
        return DelegatedClassLoader(
            this::class.java.classLoader,
            gatewayModulesClassLoader,
            folderClassLoader,
        )
    }

    private fun reinitialize() {
        logger.debug("Reinitializing class loader...")
        classLoader.set(createClassLoader())
    }

    override fun findClass(name: String): Class<*> {
        return classLoader.get().findClass(name)
    }

    override fun findResource(name: String): URL? {
        return classLoader.get().findResource(name)
    }

    override fun findResources(name: String): Enumeration<URL> {
        return classLoader.get().findResources(name)
    }

    override fun moduleAdded(module: GatewayModule) {
        gatewayModulesClassLoader.moduleAdded(module)
    }

    override fun moduleLoaded(module: GatewayModule) {
        gatewayModulesClassLoader.moduleLoaded(module)
    }

    override fun moduleStarted(module: GatewayModule) {
        gatewayModulesClassLoader.moduleStarted(module)
    }

    override fun moduleStopped(module: GatewayModule) {
        gatewayModulesClassLoader.moduleStopped(module)
    }

    override fun moduleRemoved(module: GatewayModule) {
        gatewayModulesClassLoader.moduleRemoved(module)
    }

    override fun onFileCreated(file: File) {
        if (!file.isDirectory && file.name.endsWith(".jar")) {
            logger.info("WebJar added: \"${file.name}\"")
            reinitialize()
        }
    }

    override fun onFileDeleted(file: File) {
        if (!file.isDirectory && file.name.endsWith(".jar")) {
            logger.info("WebJar removed: \"${file.name}\"")
            reinitialize()
        }
    }
}
