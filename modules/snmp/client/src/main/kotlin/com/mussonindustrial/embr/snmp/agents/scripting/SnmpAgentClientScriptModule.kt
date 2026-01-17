package com.mussonindustrial.embr.snmp.agents.scripting

import com.inductiveautomation.ignition.client.util.gui.ReadWriteOptionDialog
import com.inductiveautomation.ignition.client.util.gui.progress.Task
import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.Dataset
import com.inductiveautomation.ignition.common.model.values.QualityCode
import com.inductiveautomation.ignition.common.script.ScriptManager
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.ignition.common.script.hints.JythonElement
import com.mussonindustrial.embr.common.scripting.PyCompletableFuture
import com.mussonindustrial.embr.common.scripting.asPyCompletableFuture
import com.mussonindustrial.embr.common.scripting.asPyScriptExecutor
import com.mussonindustrial.embr.snmp.agents.rpc.SnmpAgentRpc
import com.mussonindustrial.embr.snmp.model.QualifiedOidValue
import com.mussonindustrial.embr.snmp.model.toDataset
import java.util.concurrent.CompletableFuture
import org.python.core.PyObject

class SnmpAgentClientScriptModule(rpc: SnmpAgentRpc, scriptManager: ScriptManager) :
    SnmpAgentScriptModule {

    private val overloads = SnmpAgentScriptOverloads(rpc)
    private val pyScriptExecutor = scriptManager.asPyScriptExecutor()

    companion object {
        private const val BUNDLE_PREFIX = "SnmpAgentClientScriptModule"
        private const val TIMEOUT = 0L

        init {
            BundleUtil.get()
                .addBundle(
                    SnmpAgentClientScriptModule::class.java.getSimpleName(),
                    SnmpAgentClientScriptModule::class.java.getClassLoader(),
                    SnmpAgentClientScriptModule::class.java.getName().replace('.', '/'),
                )
        }
    }

    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["agent", "oids"], types = [String::class, List::class])
    override fun readAsync(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): PyCompletableFuture<List<QualifiedOidValue>> {
        return ReadWriteOptionDialog.runReadProtectedAction<
            PyCompletableFuture<List<QualifiedOidValue>>,
            Exception,
        > {
            val future = CompletableFuture<List<QualifiedOidValue>>()
            Task.create("${SnmpAgentScriptModule.PATH}.readAsync") {
                    @Suppress("UNCHECKED_CAST")
                    overloads.read.call(args, keywords) as List<QualifiedOidValue>
                }
                .runAsync(future)
            future.asPyCompletableFuture(pyScriptExecutor)
        }
    }

    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["agent", "oids"], types = [String::class, List::class])
    override fun readBlocking(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): List<QualifiedOidValue> {
        return readAsync(args, keywords).get()
    }

    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["agent", "oids", "values"],
        types = [String::class, List::class, List::class],
    )
    override fun writeAsync(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): PyCompletableFuture<List<QualityCode>> {
        return ReadWriteOptionDialog.runWriteProtectedAction<
            PyCompletableFuture<List<QualityCode>>,
            Exception,
        > {
            val future = CompletableFuture<List<QualityCode>>()
            Task.create("${SnmpAgentScriptModule.PATH}.writeAsync") {
                    @Suppress("UNCHECKED_CAST")
                    overloads.write.call(args, keywords) as List<QualityCode>
                }
                .runAsync(future)
            future.asPyCompletableFuture(pyScriptExecutor)
        }
    }

    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["agent", "oids", "values"],
        types = [String::class, List::class, List::class],
    )
    override fun writeBlocking(args: Array<PyObject>, keywords: Array<String>): List<QualityCode> {
        return writeAsync(args, keywords).get()
    }

    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["agent", "oids"], types = [String::class, List::class])
    override fun walkAsync(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): PyCompletableFuture<List<QualifiedOidValue>> {
        return ReadWriteOptionDialog.runReadProtectedAction<
            PyCompletableFuture<List<QualifiedOidValue>>,
            Exception,
        > {
            val future = CompletableFuture<List<QualifiedOidValue>>()
            Task.create("${SnmpAgentScriptModule.PATH}.walkAsync") {
                    @Suppress("UNCHECKED_CAST")
                    overloads.walk.call(args, keywords) as List<QualifiedOidValue>
                }
                .runAsync(future)
            future.asPyCompletableFuture(pyScriptExecutor)
        }
    }

    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["agent", "oids"], types = [String::class, List::class])
    override fun walkBlocking(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): List<QualifiedOidValue> {
        return walkAsync(args, keywords).get()
    }

    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["agent", "columns", "lowerBoundIndex", "upperBoundIndex"],
        types = [String::class, List::class, String::class, String::class],
    )
    override fun readTableAsync(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): PyCompletableFuture<Dataset> {
        return ReadWriteOptionDialog.runReadProtectedAction<
            PyCompletableFuture<Dataset>,
            Exception,
        > {
            val future = CompletableFuture<Dataset>()
            Task.create("${SnmpAgentScriptModule.PATH}.walkAsync") {
                    @Suppress("UNCHECKED_CAST")
                    val results =
                        overloads.readTable.call(args, keywords) as List<List<QualifiedOidValue>>
                    results.toDataset()
                }
                .runAsync(future)
            future.asPyCompletableFuture(pyScriptExecutor)
        }
    }

    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["agent", "columns", "lowerBoundIndex", "upperBoundIndex"],
        types = [String::class, List::class, String::class, String::class],
    )
    override fun readTableBlocking(args: Array<PyObject>, keywords: Array<String>): Dataset {
        return readTableAsync(args, keywords).get()
    }

    private fun <T> Task<T>.runAsync(future: CompletableFuture<T>) {
        this.runAsync(TIMEOUT.toInt()).whenComplete { result, exception ->
            if (exception != null) {
                future.completeExceptionally(exception)
            } else {
                future.complete(result)
            }
        }
    }
}
