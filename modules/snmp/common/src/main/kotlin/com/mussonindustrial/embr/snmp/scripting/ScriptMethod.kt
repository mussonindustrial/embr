package com.mussonindustrial.embr.snmp.scripting

import com.mussonindustrial.embr.common.scripting.PyCompletableFuture
import org.python.core.PyObject

interface AsyncScript<T> {
    fun call(args: Array<PyObject>, keywords: Array<String>): PyCompletableFuture<T>
}

interface BlockingScript<T> {
    fun call(args: Array<PyObject>, keywords: Array<String>): T
}

data class ScriptMethod<T>(val async: AsyncScript<T>, val blocking: BlockingScript<T>)
