package com.mussonindustrial.ignition.embr.webassets.reflect

import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.ignition.gateway.model.GatewayModule
import com.inductiveautomation.ignition.gateway.model.ModuleManager
import com.inductiveautomation.ignition.gateway.model.ModuleObserver
import java.net.URL
import java.util.Collections
import java.util.Enumeration
import java.util.concurrent.ConcurrentHashMap

class GatewayModulesClassLoader(context: GatewayContext) :
    ClassLoader(context.javaClass.classLoader), ModuleObserver {

    override fun getName(): String {
        return parent.name + "_GatewayModulesClassLoader"
    }

    val moduleManager: ModuleManager = context.moduleManager

    private val resourceCache: MutableMap<String, URL?> = ConcurrentHashMap()
    private val resourcesCache: MutableMap<String, List<URL>> = ConcurrentHashMap()

    override fun findClass(name: String): Class<*> {
        for (module in moduleManager.gatewayModules) {
            try {

                return module.hook.javaClass.classLoader.loadClass(name)
            } catch (_: ClassNotFoundException) {}
        }
        throw ClassNotFoundException(name)
    }

    override fun findResource(name: String): URL? {
        return resourceCache.computeIfAbsent(name) {
            moduleManager.gatewayModules.firstNotNullOfOrNull {
                it.hook.javaClass.classLoader.getResource(name)
            }
        }
    }

    override fun findResources(name: String): Enumeration<URL> {
        val urls =
            resourcesCache.computeIfAbsent(name) {
                moduleManager.gatewayModules.flatMap {
                    it.hook.javaClass.classLoader.getResources(name).toList()
                }
            }
        return Collections.enumeration(urls)
    }

    fun invalidateCache() {
        resourceCache.clear()
        resourcesCache.clear()
    }

    override fun moduleAdded(module: GatewayModule) {
        invalidateCache()
    }

    override fun moduleLoaded(module: GatewayModule) {
        invalidateCache()
    }

    override fun moduleRemoved(module: GatewayModule) {
        invalidateCache()
    }
}
