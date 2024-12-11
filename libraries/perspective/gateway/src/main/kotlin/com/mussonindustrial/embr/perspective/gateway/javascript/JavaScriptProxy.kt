package com.mussonindustrial.embr.perspective.gateway.javascript

import org.python.core.PyObject

interface JavaScriptProxy {

    fun runJavaScriptBlocking(args: Array<PyObject>, keywords: Array<String>): Any?

    fun runJavaScriptAsync(args: Array<PyObject>, keywords: Array<String>)
}
