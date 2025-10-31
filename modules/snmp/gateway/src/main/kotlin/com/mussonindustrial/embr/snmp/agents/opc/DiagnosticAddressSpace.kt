package com.mussonindustrial.embr.snmp.agents.opc

import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import com.mussonindustrial.embr.snmp.opc.DeviceContextManagedAddressSpaceFragment
import com.mussonindustrial.embr.snmp.utils.removeAllNodes
import org.eclipse.milo.opcua.sdk.core.Reference
import org.eclipse.milo.opcua.sdk.server.Lifecycle
import org.eclipse.milo.opcua.sdk.server.api.AddressSpaceComposite
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilter
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilters
import org.eclipse.milo.opcua.stack.core.Identifiers
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant

class DiagnosticAddressSpace(val device: SnmpAgentDevice, composite: AddressSpaceComposite) :
    DeviceContextManagedAddressSpaceFragment(device.context.deviceContext, composite) {

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
                nodeId(root),
                qualifiedName(root),
                LocalizedText.english(root),
            )
        nodeManager.addNode(diagnosticsFolder)

        diagnosticsFolder.addReference(
            Reference(
                diagnosticsFolder.nodeId,
                Identifiers.Organizes,
                getDeviceNodeId().expanded(),
                Reference.Direction.INVERSE,
            )
        )

        addDiagnosticNode(
            diagnosticsFolder,
            "Address",
            Identifiers.String,
            AttributeFilters.getValue { DataValue(Variant(device.context.snmpSettings.address)) },
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
                DataValue(Variant(device.status == SnmpAgentDevice.Status.CONNECTED))
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
            setNodeId(nodeId("${root}${name}"))
            setBrowseName(qualifiedName(name))
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
