package com.mussonindustrial.embr.snmp.agents.opc

import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.mussonindustrial.embr.snmp.opc.DeviceContextManagedAddressSpaceFragment
import com.mussonindustrial.embr.snmp.opc.SnmpNamespace
import com.mussonindustrial.embr.snmp.opc.addHasTypeDefinition
import com.mussonindustrial.embr.snmp.opc.addNode
import com.mussonindustrial.embr.snmp.opc.addOrganizedBy
import com.mussonindustrial.embr.snmp.opc.removeAllNodes
import org.eclipse.milo.opcua.sdk.server.AddressSpaceComposite
import org.eclipse.milo.opcua.sdk.server.nodes.UaObjectNode
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
                addNode(nodeManager)
                addHasTypeDefinition(SnmpNamespace.NodesIds.SnmpAgentDeviceType.expanded())
                addOrganizedBy(rootNodeId.expanded())
            }
    }
}
