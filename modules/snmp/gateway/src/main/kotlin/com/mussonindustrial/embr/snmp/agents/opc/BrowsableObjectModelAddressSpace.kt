package com.mussonindustrial.embr.snmp.agents.opc

import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import com.mussonindustrial.embr.snmp.opc.DeviceContextManagedAddressSpaceFragment
import com.mussonindustrial.embr.snmp.utils.removeAllNodes
import org.eclipse.milo.opcua.sdk.core.Reference
import org.eclipse.milo.opcua.sdk.server.*
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode
import org.eclipse.milo.opcua.stack.core.NodeIds
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.structured.ViewDescription

class BrowsableObjectModelAddressSpace(
    val device: SnmpAgentDevice,
    composite: AddressSpaceComposite,
) : DeviceContextManagedAddressSpaceFragment(device.context.deviceContext, composite) {

    private val root = "Objects"
    private val model = device.model

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
        val objectsFolder =
            UaFolderNode(
                nodeContext,
                nodeId(root),
                qualifiedName(root),
                LocalizedText.english(root),
            )
        nodeManager.addNode(objectsFolder)

        objectsFolder.addReference(
            Reference(
                objectsFolder.nodeId,
                NodeIds.Organizes,
                deviceNodeId.expanded(),
                Reference.Direction.INVERSE,
            )
        )

        addObjectsFolder(objectsFolder, "Numeric")
        addObjectsFolder(objectsFolder, "Symbolic")
    }

    fun addObjectsFolder(folder: UaFolderNode, name: String) {
        val node =
            UaFolderNode(
                nodeContext,
                nodeId("Objects/${name}"),
                qualifiedName(name),
                LocalizedText.english(name),
            )
        nodeManager.addNode(node)

        folder.addReference(
            Reference(
                node.nodeId,
                NodeIds.Organizes,
                folder.nodeId.expanded(),
                Reference.Direction.INVERSE,
            )
        )
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
                        references.add(
                            Reference(
                                nodeId,
                                NodeIds.Organizes,
                                nodeId(descriptor.oid.numeric).expanded(),
                                Reference.Direction.FORWARD,
                            )
                        )
                    }
                nodeId("Objects/Symbolic") ->
                    model.getDescriptors(model.oids).forEach { descriptor ->
                        references.add(
                            Reference(
                                nodeId,
                                NodeIds.Organizes,
                                nodeId(descriptor.oid.symbolicName).expanded(),
                                Reference.Direction.FORWARD,
                            )
                        )
                    }
            }

            AddressSpace.ReferenceResult.of(references)
        }
    }
}
