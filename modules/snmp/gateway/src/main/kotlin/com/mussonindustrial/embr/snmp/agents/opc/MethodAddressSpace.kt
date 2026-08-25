package com.mussonindustrial.embr.snmp.agents.opc

import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import com.mussonindustrial.embr.snmp.agents.opc.nodes.ReadTableMethodNode
import com.mussonindustrial.embr.snmp.agents.opc.nodes.WalkMethodNode
import com.mussonindustrial.embr.snmp.opc.DeviceContextManagedAddressSpaceFragment
import com.mussonindustrial.embr.snmp.opc.addComponentOf
import com.mussonindustrial.embr.snmp.opc.addNode
import com.mussonindustrial.embr.snmp.opc.removeAllNodes
import org.eclipse.milo.opcua.sdk.server.AddressSpaceComposite
import org.eclipse.milo.opcua.sdk.server.Lifecycle

class MethodAddressSpace(val device: SnmpAgentDevice, composite: AddressSpaceComposite) :
    DeviceContextManagedAddressSpaceFragment(device.context.deviceContext, composite), Lifecycle {

    init {
        lifecycleManager.addStartupTask { addNodes() }
        lifecycleManager.addShutdownTask { nodeManager.removeAllNodes() }
    }

    fun addNodes() {
        ReadTableMethodNode(nodeContext, nodeId("ReadTable"), device).apply {
            addNode(nodeManager)
            addComponentOf(deviceNodeId.expanded())
        }
        WalkMethodNode(nodeContext, nodeId("Walk"), device).apply {
            addNode(nodeManager)
            addComponentOf(deviceNodeId.expanded())
        }
    }
}
