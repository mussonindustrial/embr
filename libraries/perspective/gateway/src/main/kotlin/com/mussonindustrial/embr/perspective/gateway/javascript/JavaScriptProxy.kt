package com.mussonindustrial.embr.perspective.gateway.javascript

import org.python.core.PyObject

interface JavaScriptProxy {

    fun runBlocking(args: Array<PyObject>, keywords: Array<String>): Any?

    fun runAsync(args: Array<PyObject>, keywords: Array<String>)
}
