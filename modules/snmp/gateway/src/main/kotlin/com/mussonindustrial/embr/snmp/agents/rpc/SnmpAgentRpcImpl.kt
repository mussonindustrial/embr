package com.mussonindustrial.embr.snmp.agents.rpc

import com.inductiveautomation.ignition.common.model.values.QualityCode
import com.inductiveautomation.ignition.common.project.ClientPermissionsConstants
import com.inductiveautomation.ignition.gateway.clientcomm.MutabilityMode
import com.inductiveautomation.ignition.gateway.rpc.RpcDelegate
import com.mussonindustrial.embr.snmp.SnmpGatewayContext
import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import com.mussonindustrial.embr.snmp.model.QualifiedOidValue
import com.mussonindustrial.embr.snmp.utils.toQualityCode
import org.python.core.Py.ValueError
import org.snmp4j.smi.OID
import org.snmp4j.smi.OctetString
import org.snmp4j.smi.VariableBinding

@RpcDelegate.RunsOnClient(clientPermissionId = ClientPermissionsConstants.UNRESTRICTED)
class SnmpAgentRpcImpl(val context: SnmpGatewayContext) : SnmpAgentRpc {

    private fun requireAgent(name: String): SnmpAgentDevice {
        return context.agentRegistry.get(name)
            ?: throw IllegalArgumentException("SNMP agent '$name' not found.")
    }

    @RpcDelegate.RequiredMutabilityMode(value = MutabilityMode.READ_ONLY)
    override fun read(agent: String, oids: List<String>): List<QualifiedOidValue> {
        val snmpAgent = requireAgent(agent)
        return snmpAgent.read(oids.map { oid -> VariableBinding(OID(oid)) }).map { result ->
            result.toQualifiedValue()
        }
    }

    @RpcDelegate.RequiredMutabilityMode(value = MutabilityMode.READ_WRITE)
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

    @RpcDelegate.RequiredMutabilityMode(value = MutabilityMode.READ_ONLY)
    override fun walk(agent: String, oids: List<String>): List<QualifiedOidValue> {
        val snmpAgent = requireAgent(agent)
        return snmpAgent.walk(oids.map { OID(it) }).map { it.toQualifiedValue() }
    }

    @RpcDelegate.RequiredMutabilityMode(value = MutabilityMode.READ_ONLY)
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
