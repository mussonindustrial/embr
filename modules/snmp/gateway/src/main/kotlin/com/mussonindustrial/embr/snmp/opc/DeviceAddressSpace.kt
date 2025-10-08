package com.mussonindustrial.embr.snmp.opc

import com.mussonindustrial.embr.snmp.devices.SnmpDevice
import com.mussonindustrial.embr.snmp.utils.removeAllNodes
import org.eclipse.milo.opcua.sdk.core.Reference
import org.eclipse.milo.opcua.sdk.server.AddressSpaceComposite
import org.eclipse.milo.opcua.sdk.server.Lifecycle
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode
import org.eclipse.milo.opcua.stack.core.NodeIds
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText

class DeviceAddressSpace(device: SnmpDevice, composite: AddressSpaceComposite) :
    DeviceContextManagedAddressSpaceFragment(device.context.deviceContext, composite) {

    private val deviceFolderNode =
        UaFolderNode(
            nodeContext,
            deviceNodeId,
            qualifiedName("[${name}]"),
            LocalizedText("[${name}]"),
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
