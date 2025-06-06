package com.mussonindustrial.embr.snmp.opc

import com.mussonindustrial.embr.snmp.devices.AbstractSnmpDevice
import com.mussonindustrial.embr.snmp.utils.removeAllNodes
import org.eclipse.milo.opcua.sdk.core.Reference
import org.eclipse.milo.opcua.sdk.server.Lifecycle
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode
import org.eclipse.milo.opcua.stack.core.Identifiers
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText

class DeviceAddressSpace(device: AbstractSnmpDevice<*>) :
    AbstractDeviceManagedAddressSpaceFragment(device) {

    private val deviceFolderNode =
        UaFolderNode(
            nodeContext,
            device.deviceContext.getDeviceNodeId(),
            device.deviceContext.qualifiedName("[${device.deviceContext.getName()}]"),
            LocalizedText("[${device.deviceContext.getName()}]"),
        )

    init {
        lifecycleManager.addLifecycle(
            object : Lifecycle {
                override fun startup() {
                    nodeManager.addNode(deviceFolderNode)
                    deviceFolderNode.addReference(
                        Reference(
                            deviceFolderNode.nodeId,
                            Identifiers.Organizes,
                            device.deviceContext.getRootNodeId().expanded(),
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
