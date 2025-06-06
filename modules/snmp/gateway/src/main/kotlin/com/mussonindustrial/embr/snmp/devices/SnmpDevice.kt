package com.mussonindustrial.embr.snmp.devices

import com.inductiveautomation.ignition.gateway.opcua.server.api.Device
import com.mussonindustrial.embr.snmp.requests.OidReadResult
import com.mussonindustrial.embr.snmp.requests.OidWriteResult
import org.eclipse.milo.opcua.sdk.server.api.AddressSpaceFragment
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.snmp4j.smi.VariableBinding

interface SnmpDevice : AddressSpaceFragment, Device {

    val deviceContext: SnmpDeviceContext

    fun read(reads: List<VariableBinding>): List<OidReadResult>

    fun write(writes: List<VariableBinding>): List<OidWriteResult>

    fun stripDeviceName(nodeId: NodeId): String {
        val id = nodeId.identifier.toString()
        val name = "[${getName()}]"
        return id.substring(id.indexOf(name) + name.length)
    }

    enum class Status(private val value: String) {
        DISCONNECTED("Disconnected"),
        CONNECTED("Connected");

        override fun toString(): String {
            return value
        }
    }
}
