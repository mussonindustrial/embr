package com.mussonindustrial.embr.snmp.scripting

import com.mussonindustrial.embr.common.scripting.PyCompletableFuture
import org.python.core.PyObject

interface SnmpScriptExecutor {

    fun <T> executeAsync(
        method: SnmpScriptMethod<T>,
        args: Array<PyObject>,
        keywords: Array<String>,
    ): PyCompletableFuture<T>

    fun <T> executeBlocking(
        method: SnmpScriptMethod<T>,
        args: Array<PyObject>,
        keywords: Array<String>,
    ): T
}
