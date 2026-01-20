package com.mussonindustrial.embr.snmp.agents.rpc

import com.inductiveautomation.ignition.common.model.values.QualityCode
import com.mussonindustrial.embr.snmp.SnmpGatewayContext
import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import com.mussonindustrial.embr.snmp.model.QualifiedOidValue
import com.mussonindustrial.embr.snmp.utils.toQualityCode
import org.python.core.Py.ValueError
import org.snmp4j.smi.OID
import org.snmp4j.smi.OctetString
import org.snmp4j.smi.VariableBinding

class SnmpAgentRpcImpl(val context: SnmpGatewayContext) : SnmpAgentRpc {

    private fun requireAgent(name: String): SnmpAgentDevice {
        return context.agentRegistry.get(name)
            ?: throw IllegalArgumentException("SNMP agent '$name' not found.")
    }

    override fun read(agent: String, oids: List<String>): List<QualifiedOidValue> {
        val snmpAgent = requireAgent(agent)
        return snmpAgent.read(oids.map { oid -> VariableBinding(OID(oid)) }).map {
            it.toQualifiedValue()
        }
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

    override fun walk(agent: String, oids: List<String>): List<QualifiedOidValue> {
        val snmpAgent = requireAgent(agent)
        return snmpAgent.walk(oids.map { OID(it) }).map { it.toQualifiedValue() }
    }

    override fun readTable(
        agent: String,
        columns: List<String>,
        lowerBoundIndex: String?,
        upperBoundIndex: String?,
    ): List<List<QualifiedOidValue>> {
        val snmpAgent = requireAgent(agent)
        return snmpAgent
            .readTable(
                columns.map { OID(it) },
                lowerBoundIndex.nullOrOid(),
                upperBoundIndex.nullOrOid(),
            )
            .map { it.map { binding -> binding.toQualifiedValue() } }
    }

    private fun String?.nullOrOid(): OID? {
        this?.let {
            return OID(it)
        }
        return null
    }
}
