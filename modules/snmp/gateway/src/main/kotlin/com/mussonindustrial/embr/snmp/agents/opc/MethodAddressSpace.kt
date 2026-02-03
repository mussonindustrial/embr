package com.mussonindustrial.embr.snmp.agents.opc

import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import com.mussonindustrial.embr.snmp.agents.opc.nodes.ReadTableMethodNode
import com.mussonindustrial.embr.snmp.agents.opc.nodes.WalkMethodNode
import com.mussonindustrial.embr.snmp.opc.DeviceContextManagedAddressSpaceFragment
import com.mussonindustrial.embr.snmp.utils.removeAllNodes
import org.eclipse.milo.opcua.sdk.core.Reference
import org.eclipse.milo.opcua.sdk.server.AddressSpaceComposite
import org.eclipse.milo.opcua.sdk.server.Lifecycle
import org.eclipse.milo.opcua.stack.core.NodeIds

class MethodAddressSpace(val device: SnmpAgentDevice, composite: AddressSpaceComposite) :
    DeviceContextManagedAddressSpaceFragment(device.context.deviceContext, composite), Lifecycle {

    init {
        lifecycleManager.addStartupTask { addNodes() }
        lifecycleManager.addShutdownTask { nodeManager.removeAllNodes() }
    }

    fun addNodes() {
        ReadTableMethodNode(nodeContext, nodeId("ReadTable"), device).apply {
            nodeManager.addNode(this)
            this.addReference(
                Reference(
                    this.nodeId,
                    NodeIds.HasComponent,
                    deviceNodeId.expanded(),
                    Reference.Direction.INVERSE,
                )
            )
        }
        WalkMethodNode(nodeContext, nodeId("Walk"), device).apply {
            nodeManager.addNode(this)
            this.addReference(
                Reference(
                    this.nodeId,
                    NodeIds.HasComponent,
                    deviceNodeId.expanded(),
                    Reference.Direction.INVERSE,
                )
            )
        }
    }
}
