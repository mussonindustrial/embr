package com.mussonindustrial.embr.common.reflect

import java.net.URL
import java.util.*

class DelegatedClassLoader(parent: ClassLoader, private vararg val delegates: ClassLoader) :
    ClassLoader(parent) {

    override fun findClass(name: String): Class<*> {
        for (delegate in delegates) {
            try {
                return delegate.loadClass(name)
            } catch (_: ClassNotFoundException) {}
        }
        throw ClassNotFoundException(name)
    }

    override fun findResource(name: String): URL? =
        delegates.firstNotNullOfOrNull { it.getResource(name) }

    override fun findResources(name: String): Enumeration<URL> =
        Collections.enumeration(delegates.flatMap { it.getResources(name).toList() })
}

fun withContextClassLoaders(vararg delegates: ClassLoader, block: () -> Unit) {
    val originalContextClassLoader = Thread.currentThread().contextClassLoader
    Thread.currentThread().contextClassLoader =
        DelegatedClassLoader(originalContextClassLoader, *delegates)

    block()

    Thread.currentThread().contextClassLoader = originalContextClassLoader
}
