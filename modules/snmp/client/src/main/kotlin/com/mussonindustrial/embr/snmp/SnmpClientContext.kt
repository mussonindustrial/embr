package com.mussonindustrial.embr.snmp

import com.inductiveautomation.ignition.client.gateway_interface.GatewayConnection
import com.inductiveautomation.ignition.client.model.ClientContext
import com.mussonindustrial.embr.client.EmbrClientContext
import com.mussonindustrial.embr.client.EmbrClientContextImpl
import com.mussonindustrial.embr.common.Embr
import com.mussonindustrial.embr.snmp.agents.rpc.SnmpAgentRpc

data class SnmpClientContext(val context: ClientContext) :
    EmbrClientContext by EmbrClientContextImpl(context) {
    companion object {
        lateinit var instance: SnmpClientContext
    }

    val agentRpc: SnmpAgentRpc =
        GatewayConnection.getRpcInterface(
            SnmpAgentRpc.SERIALIZER,
            Embr.SNMP.id,
            SnmpAgentRpc::class.java,
        )

    init {
        instance = this
    }
}
