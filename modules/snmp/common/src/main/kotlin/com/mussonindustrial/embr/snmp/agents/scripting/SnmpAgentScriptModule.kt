package com.mussonindustrial.embr.snmp.agents.scripting

import com.inductiveautomation.ignition.common.Dataset
import com.inductiveautomation.ignition.common.model.values.QualityCode
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.ignition.common.script.hints.JythonElement
import com.inductiveautomation.ignition.common.script.hints.NoHint
import com.mussonindustrial.embr.snmp.agents.rpc.SnmpAgentRpc
import com.mussonindustrial.embr.snmp.model.QualifiedOidValue
import com.mussonindustrial.embr.snmp.model.toDataset
import com.mussonindustrial.embr.snmp.scripting.SnmpScriptMethod
import com.mussonindustrial.embr.snmp.scripting.SnmpScriptMethodExecutor
import kotlin.reflect.typeOf
import org.python.core.PyObject

open class SnmpAgentScriptModule(
    private val methods: Methods,
    private val executor: SnmpScriptMethodExecutor,
) {

    companion object {
        @NoHint() const val PATH = "system.snmp.agent"
        @NoHint() const val BUNDLE_PREFIX = "SnmpAgentClientScriptModule"
    }

    interface Methods {
        val read: SnmpScriptMethod<List<QualifiedOidValue>>
        val write: SnmpScriptMethod<List<QualityCode>>
        val walk: SnmpScriptMethod<List<QualifiedOidValue>>
        val readTable: SnmpScriptMethod<Dataset>
    }

    class RpcDelegateMethods(val rpc: SnmpAgentRpc) : Methods {

        override val read =
            SnmpScriptMethod.of(name = "read", isWrite = false) {
                addOverload(
                    @Suppress("UNCHECKED_CAST") {
                        val agent = it["agent"] as String
                        val oids = it["oids"] as List<String>
                        rpc.read(agent, oids)
                    },
                    "agent" to typeOf<String>(),
                    "oids" to typeOf<List<String>>(),
                )
            }

        override val write =
            SnmpScriptMethod.of(name = "write", isWrite = true) {
                addOverload(
                    @Suppress("UNCHECKED_CAST") {
                        val agent = it["agent"] as String
                        val oids = it["oids"] as List<String>
                        val values = it["values"] as List<String>
                        rpc.write(agent, oids, values)
                    },
                    "agent" to typeOf<String>(),
                    "oids" to typeOf<List<String>>(),
                    "values" to typeOf<List<String>>(),
                )
            }

        override val walk =
            SnmpScriptMethod.of(name = "walk", isWrite = false) {
                addOverload(
                    @Suppress("UNCHECKED_CAST") {
                        val agent = it["agent"] as String
                        val oids = it["oids"] as List<String>
                        rpc.walk(agent, oids)
                    },
                    "agent" to typeOf<String>(),
                    "oids" to typeOf<List<String>>(),
                )
            }

        override val readTable =
            SnmpScriptMethod.of(name = "readTable", isWrite = false) {
                addOverload(
                    @Suppress("UNCHECKED_CAST") {
                        val agent = it["agent"] as String
                        val columns = it["columns"] as List<String>
                        val lowerBoundIndex = it["lowerBoundIndex"] as? String?
                        val upperBoundIndex = it["upperBoundIndex"] as? String?
                        val result = rpc.readTable(agent, columns, lowerBoundIndex, upperBoundIndex)
                        result.toDataset()
                    },
                    "agent" to typeOf<String>(),
                    "columns" to typeOf<List<String>>(),
                    "lowerBoundIndex" to typeOf<String?>(),
                    "upperBoundIndex" to typeOf<String?>(),
                )
            }
    }

    @Suppress("UNUSED")
    @NoHint()
    fun read(args: Array<PyObject>, keywords: Array<String>) =
        executor.executeBlocking(methods.read, args, keywords)

    @Suppress("UNUSED")
    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["agent", "oids"], types = [String::class, List::class])
    fun readAsync(args: Array<PyObject>, keywords: Array<String>) =
        executor.executeAsync(methods.read, args, keywords)

    @Suppress("UNUSED")
    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["agent", "oids"], types = [String::class, List::class])
    fun readBlocking(args: Array<PyObject>, keywords: Array<String>) =
        executor.executeBlocking(methods.read, args, keywords)

    @Suppress("UNUSED")
    @NoHint()
    fun write(args: Array<PyObject>, keywords: Array<String>) =
        executor.executeBlocking(methods.write, args, keywords)

    @Suppress("UNUSED")
    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["agent", "oids", "values"],
        types = [String::class, List::class, List::class],
    )
    fun writeAsync(args: Array<PyObject>, keywords: Array<String>) =
        executor.executeAsync(methods.write, args, keywords)

    @Suppress("UNUSED")
    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["agent", "oids", "values"],
        types = [String::class, List::class, List::class],
    )
    fun writeBlocking(args: Array<PyObject>, keywords: Array<String>) =
        executor.executeBlocking(methods.write, args, keywords)

    @Suppress("UNUSED")
    @NoHint()
    fun walk(args: Array<PyObject>, keywords: Array<String>) =
        executor.executeBlocking(methods.walk, args, keywords)

    @Suppress("UNUSED")
    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["agent", "oids"], types = [String::class, List::class])
    fun walkAsync(args: Array<PyObject>, keywords: Array<String>) =
        executor.executeAsync(methods.walk, args, keywords)

    @Suppress("UNUSED")
    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["agent", "oids"], types = [String::class, List::class])
    fun walkBlocking(args: Array<PyObject>, keywords: Array<String>) =
        executor.executeBlocking(methods.walk, args, keywords)

    @Suppress("UNUSED")
    @NoHint()
    fun readTable(args: Array<PyObject>, keywords: Array<String>) =
        executor.executeBlocking(methods.readTable, args, keywords)

    @Suppress("UNUSED")
    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["agent", "columns", "lowerBoundIndex", "upperBoundIndex"],
        types = [String::class, List::class, String::class, String::class],
    )
    fun readTableAsync(args: Array<PyObject>, keywords: Array<String>) =
        executor.executeAsync(methods.readTable, args, keywords)

    @Suppress("UNUSED")
    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["agent", "columns", "lowerBoundIndex", "upperBoundIndex"],
        types = [String::class, List::class, String::class, String::class],
    )
    fun readTableBlocking(args: Array<PyObject>, keywords: Array<String>) =
        executor.executeBlocking(methods.readTable, args, keywords)
}
