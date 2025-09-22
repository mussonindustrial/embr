package com.mussonindustrial.embr.snmp.opc

import com.mussonindustrial.embr.snmp.devices.SnmpDeviceImpl
import com.mussonindustrial.embr.snmp.utils.removeAllNodes
import org.eclipse.milo.opcua.sdk.core.Reference
import org.eclipse.milo.opcua.sdk.server.Lifecycle
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode
import org.eclipse.milo.opcua.stack.core.NodeIds
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText

class DeviceAddressSpace(device: SnmpDeviceImpl<*>) :
    SnmpDeviceManagedAddressSpaceFragment(device) {

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
