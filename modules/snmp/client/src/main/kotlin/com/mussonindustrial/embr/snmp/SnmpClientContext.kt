package com.mussonindustrial.embr.snmp

import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory
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

    val rpc: SnmpAgentRpc = ModuleRPCFactory.create(Embr.SNMP.id, SnmpAgentRpc::class.java)

    init {
        instance = this
    }
}
