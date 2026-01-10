package com.mussonindustrial.embr.snmp.agents.scripting

import com.mussonindustrial.embr.snmp.agents.rpc.SnmpAgentRpc

class SnmpAgentClientScriptModule(val rpc: SnmpAgentRpc) : SnmpAgentScriptModule {
    override fun read(agent: String, oids: List<String>): List<Any?> {
        return rpc.read(agent, oids)
    }

    override fun write(agent: String, oids: List<String>, values: List<String>): List<Any?> {
        return rpc.write(agent, oids, values)
    }

    override fun walk(agent: String, oids: List<String>): List<Any?> {
        return rpc.walk(agent, oids)
    }
}
