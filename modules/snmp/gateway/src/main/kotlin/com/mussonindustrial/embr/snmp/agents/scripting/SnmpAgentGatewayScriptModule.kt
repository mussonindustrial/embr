package com.mussonindustrial.embr.snmp.agents.scripting

import com.inductiveautomation.ignition.common.Dataset
import com.inductiveautomation.ignition.common.model.values.QualityCode
import com.mussonindustrial.embr.snmp.SnmpGatewayContext
import com.mussonindustrial.embr.snmp.agents.rpc.SnmpAgentRpcImpl
import com.mussonindustrial.embr.snmp.model.QualifiedOidValue
import com.mussonindustrial.embr.snmp.model.toDataset
import org.python.core.PyObject

class SnmpAgentGatewayScriptModule(context: SnmpGatewayContext) : SnmpAgentScriptModule {

    val overloads = SnmpAgentScriptOverloads(SnmpAgentRpcImpl(context))

    override fun read(args: Array<PyObject>, keywords: Array<String>): List<QualifiedOidValue> {
        @Suppress("UNCHECKED_CAST")
        return overloads.read.call(args, keywords) as List<QualifiedOidValue>
    }

    override fun write(args: Array<PyObject>, keywords: Array<String>): List<QualityCode> {
        @Suppress("UNCHECKED_CAST")
        return overloads.write.call(args, keywords) as List<QualityCode>
    }

    override fun walk(args: Array<PyObject>, keywords: Array<String>): List<QualifiedOidValue> {
        @Suppress("UNCHECKED_CAST")
        return overloads.walk.call(args, keywords) as List<QualifiedOidValue>
    }

    override fun readTable(args: Array<PyObject>, keywords: Array<String>): Dataset {
        @Suppress("UNCHECKED_CAST")
        val results = overloads.readTable.call(args, keywords) as List<List<QualifiedOidValue>>
        return results.toDataset()
    }
}
