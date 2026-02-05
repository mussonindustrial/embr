package com.mussonindustrial.embr.snmp.agents.opc

import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import com.mussonindustrial.embr.snmp.opc.DeviceContextManagedAddressSpaceFragment
import com.mussonindustrial.embr.snmp.utils.addComponentOf
import com.mussonindustrial.embr.snmp.utils.addNode
import com.mussonindustrial.embr.snmp.utils.addPropertyOf
import com.mussonindustrial.embr.snmp.utils.removeAllNodes
import org.eclipse.milo.opcua.sdk.server.AddressSpaceComposite
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode
import org.eclipse.milo.opcua.sdk.server.nodes.UaObjectNode
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilter
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilters
import org.eclipse.milo.opcua.stack.core.NodeIds
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger

class DiagnosticAddressSpace(val device: SnmpAgentDevice, composite: AddressSpaceComposite) :
    DeviceContextManagedAddressSpaceFragment(device.context.deviceContext, composite) {

    private val root = "Diagnostics"

    init {
        lifecycleManager.addStartupTask { addNodes() }
        lifecycleManager.addShutdownTask { nodeManager.removeAllNodes() }
    }

    fun addNodes() {
        val folder =
            UaObjectNode(
                    nodeContext,
                    nodeId(root),
                    qualifiedName(root),
                    LocalizedText.english(root),
                    LocalizedText.NULL_VALUE,
                    UInteger.MIN,
                    UInteger.MIN,
                )
                .apply {
                    addNode(nodeManager)
                    addComponentOf(deviceNodeId.expanded())
                }

        addDiagnosticNode(
            folder,
            "Address",
            NodeIds.String,
            AttributeFilters.getValue {
                DataValue(Variant(device.context.snmpConfig.connectivity.address))
            },
        )
        addDiagnosticNode(
            folder,
            "Status",
            NodeIds.String,
            AttributeFilters.getValue { DataValue(Variant(device.status.toString())) },
        )
        addDiagnosticNode(
            folder,
            "Connected",
            NodeIds.Boolean,
            AttributeFilters.getValue {
                DataValue(Variant(device.status == SnmpAgentDevice.Status.CONNECTED))
            },
        )
        addDiagnosticNode(
            folder,
            "PendingAsyncRequests",
            NodeIds.UInt32,
            AttributeFilters.getValue {
                DataValue(Variant(device.context.snmp.pendingAsyncRequestCount))
            },
        )
        addDiagnosticNode(
            folder,
            "PendingSyncRequests",
            NodeIds.UInt32,
            AttributeFilters.getValue {
                DataValue(Variant(device.context.snmp.pendingSyncRequestCount))
            },
        )
        addDiagnosticNode(
            folder,
            "MaxRequestPduSize",
            NodeIds.UInt32,
            AttributeFilters.getValue {
                DataValue(Variant(device.context.readTarget.maxSizeRequestPDU))
            },
        )
        addDiagnosticNode(
            folder,
            "RetryCount",
            NodeIds.UInt32,
            AttributeFilters.getValue { DataValue(Variant(device.context.readTarget.retries)) },
        )
        addDiagnosticNode(
            folder,
            "ObjectModelSize",
            NodeIds.UInt32,
            AttributeFilters.getValue { DataValue(Variant(device.model.oids.size)) },
        )
    }

    fun addDiagnosticNode(
        parent: UaNode,
        name: String,
        dataType: NodeId,
        attributeFilter: AttributeFilter,
    ) {
        UaVariableNode(
                nodeContext,
                nodeId("${root}/${name}"),
                qualifiedName(name),
                LocalizedText.english(name),
                LocalizedText.english(name),
                UInteger.MIN,
                UInteger.MIN,
            )
            .apply {
                setDataType(dataType)
                addPropertyOf(parent.nodeId.expanded())
                filterChain.addLast(attributeFilter)
            }
    }
}
