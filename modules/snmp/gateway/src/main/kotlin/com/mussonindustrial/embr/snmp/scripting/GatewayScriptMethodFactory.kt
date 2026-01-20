package com.mussonindustrial.embr.snmp.scripting

import com.mussonindustrial.embr.common.scripting.PyCompletableFuture
import com.mussonindustrial.embr.common.scripting.PyScriptExecutor
import com.mussonindustrial.embr.common.scripting.asPyCompletableFuture
import java.util.concurrent.CompletableFuture
import org.python.core.PyObject

class GatewayScriptMethodFactory(private val pyScriptExecutor: PyScriptExecutor) {

    fun <T> create(method: SnmpScriptOverload<T>): ScriptMethod<T> {

        val async =
            object : AsyncScript<T> {
                override fun call(
                    args: Array<PyObject>,
                    keywords: Array<String>,
                ): PyCompletableFuture<T> {
                    val future = CompletableFuture<T>()
                    future.completeAsync { method.call(args, keywords) }
                    return future.asPyCompletableFuture(pyScriptExecutor)
                }
            }

        val blocking =
            object : BlockingScript<T> {
                override fun call(args: Array<PyObject>, keywords: Array<String>): T =
                    method.call(args, keywords)
            }

        return ScriptMethod(async, blocking)
    }
}
