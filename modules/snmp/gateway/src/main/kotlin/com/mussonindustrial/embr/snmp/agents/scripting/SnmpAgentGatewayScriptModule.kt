package com.mussonindustrial.embr.snmp.agents.scripting

import com.inductiveautomation.ignition.common.model.values.QualifiedValue
import com.inductiveautomation.ignition.common.model.values.QualityCode
import com.mussonindustrial.embr.snmp.SnmpGatewayContext
import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import com.mussonindustrial.embr.snmp.utils.toQualityCode
import org.python.core.Py.ValueError
import org.snmp4j.smi.OID
import org.snmp4j.smi.OctetString
import org.snmp4j.smi.VariableBinding

class SnmpAgentGatewayScriptModule(private val context: SnmpGatewayContext) :
    SnmpAgentScriptModule {

    private fun requireAgent(name: String): SnmpAgentDevice {
        return context.agentRegistry.get(name)
            ?: throw IllegalArgumentException("SNMP agent '$name' not found.")
    }

    override fun read(agent: String, oids: List<String>): List<QualifiedValue> {
        val snmpAgent = requireAgent(agent)
        return snmpAgent.read(oids.map { VariableBinding(OID(it)) }).map { it.toQualifiedValue() }
    }

    override fun write(agent: String, oids: List<String>, values: List<String>): List<QualityCode> {
        val snmpAgent = requireAgent(agent)

        if (oids.size != values.size) {
            throw ValueError("Length of values does not match length of OIDs.")
        }

        return snmpAgent
            .write(
                oids.zip(values).map { (oid, value) ->
                    VariableBinding(OID(oid), OctetString(value))
                }
            )
            .map { it.statusCode.toQualityCode() }
    }

    override fun walk(agent: String, oids: List<String>): List<Any?> {
        val snmpAgent = requireAgent(agent)
        return snmpAgent.walk(oids.map { OID(it) }).map { it.value.value.value().value }
    }
}
