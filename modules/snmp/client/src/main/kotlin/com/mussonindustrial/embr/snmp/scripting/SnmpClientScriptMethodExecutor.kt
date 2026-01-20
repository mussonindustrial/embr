package com.mussonindustrial.embr.snmp.scripting

import com.inductiveautomation.ignition.client.sqltags.impl.db.ReadWriteOptionDialog
import com.mussonindustrial.embr.client.gui.runReadProtectedAction
import com.mussonindustrial.embr.client.gui.runWriteProtectedAction
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
            ReadWriteOptionDialog.getInstance().runWriteProtectedAction(action)
        } else {
            ReadWriteOptionDialog.getInstance().runReadProtectedAction(action)
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
            ReadWriteOptionDialog.getInstance()
                .runWriteProtectedAction<PyCompletableFuture<T>>(action)
        } else {
            ReadWriteOptionDialog.getInstance()
                .runReadProtectedAction<PyCompletableFuture<T>>(action)
        }
    }
}
