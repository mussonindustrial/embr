package com.mussonindustrial.embr.snmp.agents.scripting

interface SnmpAgentScriptModule {

    companion object {
        const val PATH = "system.snmp.agent"
    }

    fun read(agent: String, oids: List<String>): List<Any?>

    fun write(agent: String, oids: List<String>, values: List<String>): List<Any?>

    fun walk(agent: String, oids: List<String>): List<Any?>
}
