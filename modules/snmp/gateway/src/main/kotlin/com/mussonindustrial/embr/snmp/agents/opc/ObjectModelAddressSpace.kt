package com.mussonindustrial.embr.snmp.agents.opc

import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import com.mussonindustrial.embr.snmp.opc.DeviceContextManagedAddressSpaceFragment
import com.mussonindustrial.embr.snmp.opc.addComponentOf
import com.mussonindustrial.embr.snmp.opc.addNode
import com.mussonindustrial.embr.snmp.opc.addOrganizedBy
import com.mussonindustrial.embr.snmp.opc.organizes
import com.mussonindustrial.embr.snmp.opc.removeAllNodes
import org.eclipse.milo.opcua.sdk.server.*
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.structured.ViewDescription

class ObjectModelAddressSpace(val device: SnmpAgentDevice, composite: AddressSpaceComposite) :
    DeviceContextManagedAddressSpaceFragment(device.context.deviceContext, composite) {

    private val root = "Objects"
    private val model = device.model

    init {
        lifecycleManager.addStartupTask { addNodes() }
        lifecycleManager.addShutdownTask { nodeManager.removeAllNodes() }
    }

    fun addNodes() {
        UaFolderNode(nodeContext, nodeId(root), qualifiedName(root), LocalizedText.english(root))
            .apply {
                addNode(nodeManager)
                addComponentOf(deviceNodeId.expanded())
                addObjectsFolder(this, "Numeric")
            }
    }

    fun addObjectsFolder(folder: UaFolderNode, name: String) {
        UaFolderNode(
                nodeContext,
                nodeId("${root}/${name}"),
                qualifiedName(name),
                LocalizedText.english(name),
            )
            .apply {
                addNode(nodeManager)
                addOrganizedBy(folder.nodeId.expanded())
            }
    }

    override fun browse(
        context: AddressSpace.BrowseContext,
        view: ViewDescription,
        nodeIds: List<NodeId>,
    ): List<AddressSpace.ReferenceResult> {
        return nodeIds.map { nodeId ->
            val references = nodeManager.getReferences(nodeId)

            when (nodeId) {
                nodeId("Objects/Numeric") ->
                    model.getDescriptors(model.oids).forEach { descriptor ->
                        references += nodeId.organizes(nodeId(descriptor.oid.numeric).expanded())
                    }
            }

            AddressSpace.ReferenceResult.of(references)
        }
    }
}
