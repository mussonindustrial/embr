package com.mussonindustrial.embr.snmp.agents.rpc

import com.inductiveautomation.ignition.common.model.values.QualityCode
import com.mussonindustrial.embr.snmp.model.QualifiedOidValue

interface SnmpAgentRpc {
    fun read(agent: String, oids: List<String>): List<QualifiedOidValue>

    fun write(agent: String, oids: List<String>, values: List<String>): List<QualityCode>

    fun walk(agent: String, oids: List<String>): List<QualifiedOidValue>

    fun readTable(
        agent: String,
        columns: List<String>,
        lowerBoundIndex: String?,
        upperBoundIndex: String?,
    ): List<List<QualifiedOidValue>>
}
