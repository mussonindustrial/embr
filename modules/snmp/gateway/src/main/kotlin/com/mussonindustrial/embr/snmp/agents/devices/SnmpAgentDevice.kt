package com.mussonindustrial.embr.snmp.agents.devices

import com.inductiveautomation.ignition.gateway.opcua.server.api.Device
import com.mussonindustrial.embr.snmp.agents.context.SnmpAgentContext
import com.mussonindustrial.embr.snmp.agents.model.ObjectModel
import com.mussonindustrial.embr.snmp.model.Oid
import com.mussonindustrial.embr.snmp.model.OidValue
import org.eclipse.milo.opcua.sdk.server.AddressSpaceFragment
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode
import org.snmp4j.smi.Variable

interface SnmpAgentDevice : AddressSpaceFragment, Device {

    val context: SnmpAgentContext<*>
    val status: Status
    val model: ObjectModel

    fun read(reads: List<Oid>): List<OidValue<Variable>>

    fun write(writes: List<Pair<Oid, Variable>>): List<OidValue<StatusCode>>

    fun walk(roots: List<Oid>): List<OidValue<Variable>>

    fun readTable(
        columns: List<Oid>,
        lowerBoundIndex: Oid?,
        upperBoundIndex: Oid?,
    ): List<List<OidValue<Variable>>>

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
