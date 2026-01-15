package com.mussonindustrial.embr.snmp.agents.scripting

import com.inductiveautomation.ignition.client.util.gui.ReadWriteOptionDialog
import com.inductiveautomation.ignition.common.model.values.QualifiedValue
import com.inductiveautomation.ignition.common.model.values.QualityCode
import com.mussonindustrial.embr.snmp.agents.rpc.SnmpAgentRpc
import com.mussonindustrial.embr.snmp.model.QualifiedOidValue

class SnmpAgentClientScriptModule(private val rpc: SnmpAgentRpc) : SnmpAgentScriptModule {
    override fun read(agent: String, oids: List<String>): List<QualifiedValue> {
        return ReadWriteOptionDialog.runReadProtectedAction<List<QualifiedValue>, Exception> {
            rpc.read(agent, oids)
        }
    }

    override fun write(agent: String, oids: List<String>, values: List<String>): List<QualityCode> {
        return ReadWriteOptionDialog.runWriteProtectedAction<List<QualityCode>, Exception> {
            rpc.write(agent, oids, values)
        }
    }

    override fun walk(agent: String, oids: List<String>): List<QualifiedOidValue> {
        return ReadWriteOptionDialog.runReadProtectedAction<
            List<QualifiedOidValue>,
            Exception,
        > @Throws() { rpc.walk(agent, oids) }
    }
}
