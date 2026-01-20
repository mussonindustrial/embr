package com.mussonindustrial.embr.snmp.scripting

import com.inductiveautomation.factorypmi.application.runtime.ClientGatewayConnection
import com.inductiveautomation.ignition.client.gateway_interface.GatewayConnectionManager
import com.mussonindustrial.embr.common.scripting.PyCompletableFuture
import com.mussonindustrial.embr.common.scripting.PyScriptExecutor
import com.mussonindustrial.embr.common.scripting.asPyCompletableFuture
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import javax.swing.SwingWorker
import org.python.core.PyObject

class SnmpClientScriptMethodExecutor(private val pyScriptExecutor: PyScriptExecutor) :
    SnmpScriptMethodExecutor {

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
            runWriteProtectedAction<T>(action)
        } else {
            runReadProtectedAction<T>(action)
        }
    }

    class AsyncWorker<T>(
        val future: CompletableFuture<T>,
        val method: SnmpScriptMethod<T>,
        val args: Array<PyObject>,
        val keywords: Array<String>,
    ) : SwingWorker<T, Unit>() {
        override fun doInBackground(): T {
            return method.overload.call(args, keywords)
        }

        override fun done() {
            future.complete(get())
        }
    }

    override fun <T> executeAsync(
        method: SnmpScriptMethod<T>,
        args: Array<PyObject>,
        keywords: Array<String>,
    ): PyCompletableFuture<T> {

        val action = {
            val future = CompletableFuture<T>()
            future.orTimeout(TIMEOUT, TimeUnit.SECONDS)
            val worker = AsyncWorker(future, method, args, keywords)
            worker.execute()
            future.asPyCompletableFuture(pyScriptExecutor)
        }

        return if (method.isWrite) {
            runWriteProtectedAction<PyCompletableFuture<T>>(action)
        } else {
            runReadProtectedAction<PyCompletableFuture<T>>(action)
        }
    }

    fun <T> runWriteProtectedAction(block: () -> T): T {
        if (
            GatewayConnectionManager.getInstance().connectionMode !=
                ClientGatewayConnection.MODE_FULL
        ) {
            throw Exception("Gateway mode is not read/write.")
        }
        return block()
    }

    fun <T> runReadProtectedAction(block: () -> T): T {
        if (
            GatewayConnectionManager.getInstance().connectionMode ==
                ClientGatewayConnection.MODE_DISCONNECTED
        ) {
            throw Exception("Gateway mode is not read.")
        }
        return block()
    }
}
