package com.mussonindustrial.embr.snmp.agents

import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import java.util.concurrent.ConcurrentHashMap

class SnmpAgentRegistry {
    val agents = ConcurrentHashMap<String, SnmpAgentDevice>()

    fun register(agent: SnmpAgentDevice) {
        agents[agent.context.deviceContext.name] = agent
    }

    fun unregister(agent: SnmpAgentDevice) {
        agents.remove(agent.context.deviceContext.name)
    }

    fun get(name: String): SnmpAgentDevice? {
        return agents[name]
    }
}
