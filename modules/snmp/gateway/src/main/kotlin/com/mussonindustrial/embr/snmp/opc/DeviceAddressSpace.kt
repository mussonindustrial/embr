package com.mussonindustrial.embr.snmp.opc

import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.mussonindustrial.embr.snmp.utils.removeAllNodes
import org.eclipse.milo.opcua.sdk.core.Reference
import org.eclipse.milo.opcua.sdk.server.AddressSpaceComposite
import org.eclipse.milo.opcua.sdk.server.Lifecycle
import org.eclipse.milo.opcua.sdk.server.nodes.UaObjectNode
import org.eclipse.milo.opcua.stack.core.NodeIds
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger

class DeviceAddressSpace(deviceContext: DeviceContext, composite: AddressSpaceComposite) :
    DeviceContextManagedAddressSpaceFragment(deviceContext, composite) {

    private val deviceFolderNode =
        UaObjectNode(
            nodeContext,
            deviceNodeId,
            qualifiedName("[${name}]"),
            LocalizedText("[${name}]"),
            LocalizedText("SNMP Agent Device"),
            UInteger.MIN,
            UInteger.MIN,
        )

    init {
        lifecycleManager.addLifecycle(
            object : Lifecycle {
                override fun startup() {
                    nodeManager.addNode(deviceFolderNode)
                    deviceFolderNode.addReference(
                        Reference(
                            deviceFolderNode.nodeId,
                            NodeIds.Organizes,
                            getRootNodeId().expanded(),
                            Reference.Direction.INVERSE,
                        )
                    )
                }

                override fun shutdown() {
                    nodeManager.removeAllNodes()
                }
            }
        )
    }
}
