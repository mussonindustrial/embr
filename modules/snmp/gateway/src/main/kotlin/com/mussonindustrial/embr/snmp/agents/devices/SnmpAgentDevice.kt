package com.mussonindustrial.embr.snmp.agents.devices

import com.inductiveautomation.ignition.gateway.opcua.server.api.Device
import com.mussonindustrial.embr.snmp.agents.context.SnmpAgentContext
import com.mussonindustrial.embr.snmp.requests.OidReadResult
import com.mussonindustrial.embr.snmp.requests.OidWriteResult
import org.eclipse.milo.opcua.sdk.server.AddressSpaceFragment
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.snmp4j.smi.VariableBinding

interface SnmpAgentDevice : AddressSpaceFragment, Device {

    val context: SnmpAgentContext<*>
    val status: Status

    fun read(reads: List<VariableBinding>): List<OidReadResult>

    fun write(writes: List<VariableBinding>): List<OidWriteResult>

    fun stripDeviceName(nodeId: NodeId): String {
        val id = nodeId.identifier.toString()
        val name = "[${context.deviceContext.name}]"
        return id.substring(id.indexOf(name) + name.length)
    }

    enum class Status(private val value: String) {
        UNKNOWN("Unknown"),
        DISCONNECTED("Disconnected"),
        CONNECTED("Connected"),
        FAULTED("Faulted");

        override fun toString(): String {
            return value
        }
    }
}
