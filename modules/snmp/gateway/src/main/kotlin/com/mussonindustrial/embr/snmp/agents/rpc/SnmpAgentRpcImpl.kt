package com.mussonindustrial.embr.snmp.agents.rpc

import com.inductiveautomation.ignition.common.model.values.QualifiedValue
import com.inductiveautomation.ignition.common.model.values.QualityCode
import com.inductiveautomation.ignition.common.project.ClientPermissionsConstants
import com.inductiveautomation.ignition.gateway.clientcomm.MutabilityMode
import com.inductiveautomation.ignition.gateway.rpc.RpcDelegate
import com.mussonindustrial.embr.snmp.SnmpGatewayContext
import com.mussonindustrial.embr.snmp.agents.scripting.SnmpAgentGatewayScriptModule

@RpcDelegate.RunsOnClient(clientPermissionId = ClientPermissionsConstants.UNRESTRICTED)
class SnmpAgentRpcImpl(val context: SnmpGatewayContext) : SnmpAgentRpc {

    val scriptModule = SnmpAgentGatewayScriptModule(context)

    @RpcDelegate.RequiredMutabilityMode(value = MutabilityMode.READ_ONLY)
    override fun read(agent: String, oids: List<String>): List<QualifiedValue> {
        return scriptModule.read(agent, oids)
    }

    @RpcDelegate.RequiredMutabilityMode(value = MutabilityMode.READ_WRITE)
    override fun write(agent: String, oids: List<String>, values: List<String>): List<QualityCode> {
        return scriptModule.write(agent, oids, values)
    }

    @RpcDelegate.RequiredMutabilityMode(value = MutabilityMode.READ_ONLY)
    override fun walk(agent: String, oids: List<String>): List<Any?> {
        return scriptModule.walk(agent, oids)
    }
}
