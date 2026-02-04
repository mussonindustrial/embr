package com.mussonindustrial.embr.snmp.agents.opc

import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.mussonindustrial.embr.snmp.agents.opc.types.SnmpAgentDeviceType
import com.mussonindustrial.embr.snmp.opc.DeviceContextManagedAddressSpaceFragment
import com.mussonindustrial.embr.snmp.opc.SnmpNamespace
import com.mussonindustrial.embr.snmp.utils.removeAllNodes
import org.eclipse.milo.opcua.sdk.core.Reference
import org.eclipse.milo.opcua.sdk.server.AddressSpaceComposite
import org.eclipse.milo.opcua.sdk.server.nodes.UaObjectNode
import org.eclipse.milo.opcua.stack.core.NodeIds
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger

class DeviceAddressSpace(deviceContext: DeviceContext, composite: AddressSpaceComposite) :
    DeviceContextManagedAddressSpaceFragment(deviceContext, composite) {

    init {
        lifecycleManager.addStartupTask { addNodes() }
        lifecycleManager.addShutdownTask { nodeManager.removeAllNodes() }
    }

    fun addNodes() {
        UaObjectNode(
                nodeContext,
                deviceNodeId,
                qualifiedName("[${name}]"),
                LocalizedText("[${name}]"),
                LocalizedText("SNMP Agent Device"),
                UInteger.MIN,
                UInteger.MIN,
            )
            .apply {
                nodeManager.addNode(this)
                addReference(
                    Reference(
                        this.nodeId,
                        NodeIds.HasTypeDefinition,
                        SnmpNamespace.NodesIds.SnmpAgentDeviceType.expanded(),
                        Reference.Direction.FORWARD,
                    )
                )
                addReference(
                    Reference(
                        this.nodeId,
                        NodeIds.Organizes,
                        rootNodeId.expanded(),
                        Reference.Direction.INVERSE,
                    )
                )
            }
    }
}
