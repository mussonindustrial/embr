package com.mussonindustrial.embr.snmp

import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.mussonindustrial.embr.common.Embr
import com.mussonindustrial.embr.designer.EmbrDesignerContext
import com.mussonindustrial.embr.designer.EmbrDesignerContextImpl
import com.mussonindustrial.embr.snmp.agents.rpc.SnmpAgentRpc

data class SnmpDesignerContext(val context: DesignerContext) :
    EmbrDesignerContext by EmbrDesignerContextImpl(context) {
    companion object {
        lateinit var instance: SnmpDesignerContext
    }

    val rpc: SnmpAgentRpc = ModuleRPCFactory.create(Embr.SNMP.id, SnmpAgentRpc::class.java)

    init {
        instance = this
    }
}
