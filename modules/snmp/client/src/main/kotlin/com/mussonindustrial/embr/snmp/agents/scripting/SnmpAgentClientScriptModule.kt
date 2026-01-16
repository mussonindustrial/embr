package com.mussonindustrial.embr.snmp.agents.scripting

import com.inductiveautomation.ignition.client.util.gui.ReadWriteOptionDialog
import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.Dataset
import com.inductiveautomation.ignition.common.model.values.QualityCode
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.ignition.common.script.hints.JythonElement
import com.mussonindustrial.embr.snmp.agents.rpc.SnmpAgentRpc
import com.mussonindustrial.embr.snmp.model.QualifiedOidValue
import com.mussonindustrial.embr.snmp.model.toDataset
import org.python.core.PyObject

class SnmpAgentClientScriptModule(private val rpc: SnmpAgentRpc) : SnmpAgentScriptModule {

    private val overloads = SnmpAgentScriptOverloads(rpc)

    companion object {
        private const val BUNDLE_PREFIX = "SnmpAgentClientScriptModule"

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
    override fun read(args: Array<PyObject>, keywords: Array<String>): List<QualifiedOidValue> {
        return ReadWriteOptionDialog.runReadProtectedAction<List<QualifiedOidValue>, Exception> {
            @Suppress("UNCHECKED_CAST")
            overloads.read.call(args, keywords) as List<QualifiedOidValue>
        }
    }

    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["agent", "oids", "values"],
        types = [String::class, List::class, List::class],
    )
    override fun write(args: Array<PyObject>, keywords: Array<String>): List<QualityCode> {
        return ReadWriteOptionDialog.runWriteProtectedAction<List<QualityCode>, Exception> {
            @Suppress("UNCHECKED_CAST")
            overloads.write.call(args, keywords) as List<QualityCode>
        }
    }

    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["agent", "oids"], types = [String::class, List::class])
    override fun walk(args: Array<PyObject>, keywords: Array<String>): List<QualifiedOidValue> {
        return ReadWriteOptionDialog.runReadProtectedAction<List<QualifiedOidValue>, Exception> {
            @Suppress("UNCHECKED_CAST")
            overloads.walk.call(args, keywords) as List<QualifiedOidValue>
        }
    }

    @JythonElement(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["agent", "columns", "lowerBoundIndex", "upperBoundIndex"],
        types = [String::class, List::class, String::class, String::class],
    )
    override fun readTable(args: Array<PyObject>, keywords: Array<String>): Dataset {
        val results =
            ReadWriteOptionDialog.runReadProtectedAction<List<List<QualifiedOidValue>>, Exception> {
                @Suppress("UNCHECKED_CAST")
                overloads.readTable.call(args, keywords) as List<List<QualifiedOidValue>>
            }
        return results.toDataset()
    }
}
