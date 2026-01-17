package com.mussonindustrial.embr.snmp.agents.scripting

import com.inductiveautomation.ignition.common.Dataset
import com.inductiveautomation.ignition.common.model.values.QualityCode
import com.inductiveautomation.ignition.common.script.ScriptManager
import com.mussonindustrial.embr.common.scripting.PyCompletableFuture
import com.mussonindustrial.embr.common.scripting.asPyCompletableFuture
import com.mussonindustrial.embr.common.scripting.asPyScriptExecutor
import com.mussonindustrial.embr.snmp.SnmpGatewayContext
import com.mussonindustrial.embr.snmp.agents.rpc.SnmpAgentRpcImpl
import com.mussonindustrial.embr.snmp.model.QualifiedOidValue
import com.mussonindustrial.embr.snmp.model.toDataset
import java.util.concurrent.CompletableFuture
import org.python.core.PyObject

class SnmpAgentGatewayScriptModule(
    private val context: SnmpGatewayContext,
    scriptManager: ScriptManager,
) : SnmpAgentScriptModule {

    val overloads = SnmpAgentScriptOverloads(SnmpAgentRpcImpl(context))
    val pyScriptExecutor = scriptManager.asPyScriptExecutor()

    override fun readAsync(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): PyCompletableFuture<List<QualifiedOidValue>> {
        val future = CompletableFuture<List<QualifiedOidValue>>()
        future.completeAsync {
            @Suppress("UNCHECKED_CAST")
            overloads.read.call(args, keywords) as List<QualifiedOidValue>
        }
        return future.asPyCompletableFuture(pyScriptExecutor)
    }

    override fun readBlocking(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): List<QualifiedOidValue> {
        return readAsync(args, keywords).get()
    }

    override fun writeAsync(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): PyCompletableFuture<List<QualityCode>> {
        val future = CompletableFuture<List<QualityCode>>()
        future.completeAsync {
            @Suppress("UNCHECKED_CAST")
            overloads.write.call(args, keywords) as List<QualityCode>
        }
        return future.asPyCompletableFuture(pyScriptExecutor)
    }

    override fun writeBlocking(args: Array<PyObject>, keywords: Array<String>): List<QualityCode> {
        return writeAsync(args, keywords).get()
    }

    override fun walkAsync(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): PyCompletableFuture<List<QualifiedOidValue>> {
        val future = CompletableFuture<List<QualifiedOidValue>>()
        future.completeAsync {
            @Suppress("UNCHECKED_CAST")
            overloads.walk.call(args, keywords) as List<QualifiedOidValue>
        }
        return future.asPyCompletableFuture(pyScriptExecutor)
    }

    override fun walkBlocking(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): List<QualifiedOidValue> {
        return walkAsync(args, keywords).get()
    }

    override fun readTableAsync(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): PyCompletableFuture<Dataset> {
        val future = CompletableFuture<Dataset>()
        future.completeAsync {
            @Suppress("UNCHECKED_CAST")
            val results = overloads.readTable.call(args, keywords) as List<List<QualifiedOidValue>>
            results.toDataset()
        }
        return future.asPyCompletableFuture(pyScriptExecutor)
    }

    override fun readTableBlocking(args: Array<PyObject>, keywords: Array<String>): Dataset {
        return readTableAsync(args, keywords).get()
    }
}
