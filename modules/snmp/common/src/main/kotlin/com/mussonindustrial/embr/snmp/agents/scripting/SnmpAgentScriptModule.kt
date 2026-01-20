package com.mussonindustrial.embr.snmp.agents.scripting

import com.inductiveautomation.ignition.common.Dataset
import com.inductiveautomation.ignition.common.model.values.QualityCode
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.ignition.common.script.hints.JythonElement
import com.inductiveautomation.ignition.common.script.hints.NoHint
import com.mussonindustrial.embr.snmp.model.QualifiedOidValue
import com.mussonindustrial.embr.snmp.scripting.ScriptMethod
import org.python.core.PyObject

open class SnmpAgentScriptModule(private val methods: Methods) {

    companion object {
        const val PATH = "system.snmp.agent"
        private const val BUNDLE_PREFIX = "SnmpAgentClientScriptModule"
    }

    data class Methods(
        val read: ScriptMethod<List<QualifiedOidValue>>,
        val write: ScriptMethod<List<QualityCode>>,
        val walk: ScriptMethod<List<QualifiedOidValue>>,
        val readTable: ScriptMethod<Dataset>,
    )

    @Suppress("UNUSED")
    @NoHint()
    fun read(args: Array<PyObject>, keywords: Array<String>) =
        methods.read.blocking.call(args, keywords)

    @Suppress("UNUSED")
    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["agent", "oids"], types = [String::class, List::class])
    fun readAsync(args: Array<PyObject>, keywords: Array<String>) =
        methods.read.async.call(args, keywords)

    @Suppress("UNUSED")
    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["agent", "oids"], types = [String::class, List::class])
    fun readBlocking(args: Array<PyObject>, keywords: Array<String>) =
        methods.read.blocking.call(args, keywords)

    @Suppress("UNUSED")
    @NoHint()
    fun write(args: Array<PyObject>, keywords: Array<String>) =
        methods.write.blocking.call(args, keywords)

    @Suppress("UNUSED")
    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["agent", "oids", "values"],
        types = [String::class, List::class, List::class],
    )
    fun writeAsync(args: Array<PyObject>, keywords: Array<String>) =
        methods.write.async.call(args, keywords)

    @Suppress("UNUSED")
    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["agent", "oids", "values"],
        types = [String::class, List::class, List::class],
    )
    fun writeBlocking(args: Array<PyObject>, keywords: Array<String>) =
        methods.write.blocking.call(args, keywords)

    @Suppress("UNUSED")
    @NoHint()
    fun walk(args: Array<PyObject>, keywords: Array<String>) =
        methods.walk.blocking.call(args, keywords)

    @Suppress("UNUSED")
    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["agent", "oids"], types = [String::class, List::class])
    fun walkAsync(args: Array<PyObject>, keywords: Array<String>) =
        methods.walk.async.call(args, keywords)

    @Suppress("UNUSED")
    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["agent", "oids"], types = [String::class, List::class])
    fun walkBlocking(args: Array<PyObject>, keywords: Array<String>) =
        methods.walk.blocking.call(args, keywords)

    @Suppress("UNUSED")
    @NoHint()
    fun readTable(args: Array<PyObject>, keywords: Array<String>) =
        methods.readTable.blocking.call(args, keywords)

    @Suppress("UNUSED")
    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["agent", "columns", "lowerBoundIndex", "upperBoundIndex"],
        types = [String::class, List::class, String::class, String::class],
    )
    fun readTableAsync(args: Array<PyObject>, keywords: Array<String>) =
        methods.readTable.async.call(args, keywords)

    @Suppress("UNUSED")
    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["agent", "columns", "lowerBoundIndex", "upperBoundIndex"],
        types = [String::class, List::class, String::class, String::class],
    )
    fun readTableBlocking(args: Array<PyObject>, keywords: Array<String>) =
        methods.readTable.blocking.call(args, keywords)
}
