package com.mussonindustrial.embr.snmp.agents.rpc

import com.inductiveautomation.ignition.common.model.values.QualityCode
import com.inductiveautomation.ignition.common.project.ClientPermissionsConstants
import com.inductiveautomation.ignition.gateway.clientcomm.MutabilityMode
import com.inductiveautomation.ignition.gateway.rpc.RpcDelegate
import com.mussonindustrial.embr.snmp.SnmpGatewayContext
import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import com.mussonindustrial.embr.snmp.model.Oid
import com.mussonindustrial.embr.snmp.model.QualifiedOidValue
import com.mussonindustrial.embr.snmp.model.Snmp4jOid
import com.mussonindustrial.embr.snmp.utils.toQualifiedValue
import com.mussonindustrial.embr.snmp.utils.toQualityCode
import org.python.core.Py.ValueError

@RpcDelegate.RunsOnClient(clientPermissionId = ClientPermissionsConstants.UNRESTRICTED)
class SnmpAgentRpcImpl(val context: SnmpGatewayContext) : SnmpAgentRpc {

    private fun requireAgent(name: String): SnmpAgentDevice {
        return context.agentRegistry.get(name)
            ?: throw IllegalArgumentException("SNMP agent '$name' not found.")
    }

    @RpcDelegate.RequiredMutabilityMode(value = MutabilityMode.READ_ONLY)
    override fun read(agent: String, oids: List<String>): List<QualifiedOidValue> {
        val snmpAgent = requireAgent(agent)
        return snmpAgent.model.read(oids.map { Snmp4jOid(it) }).map { it.toQualifiedValue() }
    }

    @RpcDelegate.RequiredMutabilityMode(value = MutabilityMode.READ_WRITE)
    override fun write(agent: String, oids: List<String>, values: List<Any?>): List<QualityCode> {
        val snmpAgent = requireAgent(agent)

        if (oids.size != values.size) {
            throw ValueError("Length of values does not match length of OIDs.")
        }

        return snmpAgent.model
            .write(oids.zip(values).map { (oid, value) -> Snmp4jOid(oid) to value })
            .map { it.value.toQualityCode() }
    }

    @RpcDelegate.RequiredMutabilityMode(value = MutabilityMode.READ_ONLY)
    override fun walk(agent: String, oids: List<String>): List<QualifiedOidValue> {
        val snmpAgent = requireAgent(agent)
        return snmpAgent.model.walk(oids.map { Snmp4jOid(it) }).map { it.toQualifiedValue() }
    }

    @RpcDelegate.RequiredMutabilityMode(value = MutabilityMode.READ_ONLY)
    override fun readTable(
        agent: String,
        columns: List<String>,
        lowerBoundIndex: String?,
        upperBoundIndex: String?,
    ): List<List<QualifiedOidValue>> {
        val snmpAgent = requireAgent(agent)
        return snmpAgent.model
            .readTable(
                columns.map { Snmp4jOid(it) },
                lowerBoundIndex.nullOrOid(),
                upperBoundIndex.nullOrOid(),
            )
            .map { results -> results.map { it.toQualifiedValue() } }
    }

    private fun String?.nullOrOid(): Oid? {
        this?.let {
            return Snmp4jOid(it)
        }
        return null
    }
}
