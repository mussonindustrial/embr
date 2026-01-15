package com.mussonindustrial.embr.snmp.agents.rpc

import com.inductiveautomation.ignition.common.model.values.QualifiedValue
import com.inductiveautomation.ignition.common.model.values.QualityCode
import com.inductiveautomation.ignition.common.rpc.RpcInterface
import com.inductiveautomation.ignition.common.rpc.proto.ProtoRpcSerializer
import com.mussonindustrial.embr.snmp.model.BasicQualifiedOidValue
import com.mussonindustrial.embr.snmp.model.QualifiedOidValue

@RpcInterface(packageId = "snmp-agent")
interface SnmpAgentRpc {
    companion object {
        val SERIALIZER: ProtoRpcSerializer =
            ProtoRpcSerializer.newBuilder()
                .addBinaryAdapter(
                    BasicQualifiedOidValue::class.java,
                    BasicQualifiedOidValue::encode,
                    BasicQualifiedOidValue::decode,
                )
                .build()
    }

    fun read(agent: String, oids: List<String>): List<QualifiedValue>

    fun write(agent: String, oids: List<String>, values: List<String>): List<QualityCode>

    fun walk(agent: String, oids: List<String>): List<QualifiedOidValue>
}
