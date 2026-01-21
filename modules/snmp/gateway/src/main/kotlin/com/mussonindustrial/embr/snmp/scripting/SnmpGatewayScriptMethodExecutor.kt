package com.mussonindustrial.embr.snmp.scripting

import com.mussonindustrial.embr.common.scripting.PyCompletableFuture
import com.mussonindustrial.embr.common.scripting.PyScriptExecutor
import com.mussonindustrial.embr.common.scripting.asPyCompletableFuture
import java.util.concurrent.CompletableFuture
import org.python.core.PyObject

class SnmpGatewayScriptMethodExecutor(private val pyScriptExecutor: PyScriptExecutor) :
    SnmpScriptMethodExecutor {

    override fun <T> executeBlocking(
        method: SnmpScriptMethod<T>,
        args: Array<PyObject>,
        keywords: Array<String>,
    ): T {
        return method.overload.call(args, keywords)
    }

    override fun <T> executeAsync(
        method: SnmpScriptMethod<T>,
        args: Array<PyObject>,
        keywords: Array<String>,
    ): PyCompletableFuture<T> {
        val future = CompletableFuture<T>()
        future.completeAsync { method.overload.call(args, keywords) }
        return future.asPyCompletableFuture(pyScriptExecutor)
    }
}
