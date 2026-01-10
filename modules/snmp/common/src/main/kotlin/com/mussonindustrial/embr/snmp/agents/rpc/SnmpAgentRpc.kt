package com.mussonindustrial.embr.snmp.agents.rpc

import com.inductiveautomation.ignition.common.rpc.RpcInterface
import com.inductiveautomation.ignition.common.rpc.proto.ProtoRpcSerializer

@RpcInterface(packageId = "snmp-agent")
interface SnmpAgentRpc {
    companion object {
        val SERIALIZER: ProtoRpcSerializer = ProtoRpcSerializer.newBuilder().build()
    }

    fun read(agent: String, oids: List<String>): List<Any?>

    fun write(agent: String, oids: List<String>, values: List<String>): List<Any?>

    fun walk(agent: String, oids: List<String>): List<Any?>
}
