package com.mussonindustrial.embr.snmp.scripting

import com.inductiveautomation.ignition.client.util.gui.ReadWriteOptionDialog
import com.inductiveautomation.ignition.client.util.gui.progress.Task
import com.mussonindustrial.embr.common.scripting.PyCompletableFuture
import com.mussonindustrial.embr.common.scripting.PyScriptExecutor
import com.mussonindustrial.embr.common.scripting.asPyCompletableFuture
import com.mussonindustrial.embr.snmp.agents.scripting.SnmpAgentScriptModule
import java.util.concurrent.CompletableFuture
import org.python.core.PyObject

class SnmpClientScriptExecutor(private val pyScriptExecutor: PyScriptExecutor) :
    SnmpScriptExecutor {

    companion object {
        const val TIMEOUT = 120L
    }

    override fun <T> executeBlocking(
        method: SnmpScriptMethod<T>,
        args: Array<PyObject>,
        keywords: Array<String>,
    ): T {
        val action = { method.overload.call(args, keywords) }

        return if (method.isWrite) {
            ReadWriteOptionDialog.runWriteProtectedAction<T, Exception>(action)
        } else {
            ReadWriteOptionDialog.runReadProtectedAction<T, Exception>(action)
        }
    }

    override fun <T> executeAsync(
        method: SnmpScriptMethod<T>,
        args: Array<PyObject>,
        keywords: Array<String>,
    ): PyCompletableFuture<T> {

        val action = {
            val future = CompletableFuture<T>()

            Task.create("${SnmpAgentScriptModule.PATH}.${method.name}") {
                    method.overload.call(args, keywords)
                }
                .runAsync(TIMEOUT.toInt())
                .whenComplete { result, exception ->
                    if (exception != null) {
                        future.completeExceptionally(exception)
                    } else {
                        future.complete(result)
                    }
                }

            future.asPyCompletableFuture(pyScriptExecutor)
        }

        return if (method.isWrite) {
            ReadWriteOptionDialog.runWriteProtectedAction<PyCompletableFuture<T>, Exception>(action)
        } else {
            ReadWriteOptionDialog.runReadProtectedAction<PyCompletableFuture<T>, Exception>(action)
        }
    }
}
