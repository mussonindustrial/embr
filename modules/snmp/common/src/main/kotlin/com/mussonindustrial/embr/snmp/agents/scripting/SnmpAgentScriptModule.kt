package com.mussonindustrial.embr.snmp.agents.scripting

import com.inductiveautomation.ignition.common.model.values.QualityCode
import com.mussonindustrial.embr.snmp.model.QualifiedOidValue

interface SnmpAgentScriptModule {

    companion object {
        const val PATH = "system.snmp.agent"
    }

    fun read(agent: String, oids: List<String>): List<QualifiedOidValue>

    fun write(agent: String, oids: List<String>, values: List<String>): List<QualityCode>

    fun walk(agent: String, oids: List<String>): List<QualifiedOidValue>
}
