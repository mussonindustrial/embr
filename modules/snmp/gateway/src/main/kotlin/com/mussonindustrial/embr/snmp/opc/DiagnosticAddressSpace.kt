package com.mussonindustrial.embr.snmp.opc

import com.mussonindustrial.embr.snmp.devices.AbstractSnmpDevice
import com.mussonindustrial.embr.snmp.devices.SnmpDevice
import com.mussonindustrial.embr.snmp.utils.removeAllNodes
import org.eclipse.milo.opcua.sdk.core.Reference
import org.eclipse.milo.opcua.sdk.server.Lifecycle
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilter
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilters
import org.eclipse.milo.opcua.stack.core.Identifiers
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant

class DiagnosticAddressSpace(device: AbstractSnmpDevice<*>) :
    AbstractDeviceManagedAddressSpaceFragment(device) {

    private val root = "[Diagnostics]"

    init {
        lifecycleManager.addLifecycle(
            object : Lifecycle {
                override fun startup() {
                    addNodes()
                }

                override fun shutdown() {
                    nodeManager.removeAllNodes()
                }
            }
        )
    }

    fun addNodes() {
        val diagnosticsFolder =
            UaFolderNode(
                nodeContext,
                device.deviceContext.nodeId(root),
                device.deviceContext.qualifiedName(root),
                LocalizedText.english(root),
            )
        nodeManager.addNode(diagnosticsFolder)

        diagnosticsFolder.addReference(
            Reference(
                diagnosticsFolder.nodeId,
                Identifiers.Organizes,
                device.deviceContext.getDeviceNodeId().expanded(),
                Reference.Direction.INVERSE,
            )
        )

        addDiagnosticNode(
            diagnosticsFolder,
            "Hostname",
            Identifiers.String,
            AttributeFilters.getValue { DataValue(Variant(device.snmpSettings.hostname)) },
        )
        addDiagnosticNode(
            diagnosticsFolder,
            "Status",
            Identifiers.String,
            AttributeFilters.getValue { DataValue(Variant(device.status.toString())) },
        )
        addDiagnosticNode(
            diagnosticsFolder,
            "Connected",
            Identifiers.Boolean,
            AttributeFilters.getValue {
                DataValue(Variant(device.status == SnmpDevice.Status.CONNECTED))
            },
        )
    }

    fun addDiagnosticNode(
        folder: UaFolderNode,
        name: String,
        dataType: NodeId,
        attributeFilter: AttributeFilter,
    ) {
        UaVariableNode.UaVariableNodeBuilder(nodeContext).run {
            setNodeId(device.deviceContext.nodeId("${root}/${name}"))
            setBrowseName(device.deviceContext.qualifiedName(name))
            setDisplayName(LocalizedText.english(name))
            setDataType(dataType)
            addReference(
                Reference(
                    nodeId,
                    Identifiers.HasComponent,
                    folder.nodeId.expanded(),
                    Reference.Direction.INVERSE,
                )
            )
            addAttributeFilter(attributeFilter)
            buildAndAdd()
        }
    }
}
