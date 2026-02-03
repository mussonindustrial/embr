package com.mussonindustrial.embr.snmp.agents.opc

import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import com.mussonindustrial.embr.snmp.agents.opc.types.OidValueType
import com.mussonindustrial.embr.snmp.model.Snmp4jOid
import com.mussonindustrial.embr.snmp.opc.DeviceContextManagedAddressSpaceFragment
import com.mussonindustrial.embr.snmp.utils.removeAllNodes
import org.eclipse.milo.opcua.sdk.core.Reference
import org.eclipse.milo.opcua.sdk.core.ValueRank
import org.eclipse.milo.opcua.sdk.server.AddressSpaceComposite
import org.eclipse.milo.opcua.sdk.server.Lifecycle
import org.eclipse.milo.opcua.sdk.server.methods.MethodInvocationHandler
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode
import org.eclipse.milo.opcua.stack.core.NodeIds
import org.eclipse.milo.opcua.stack.core.types.builtin.*
import org.eclipse.milo.opcua.stack.core.types.structured.Argument
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodResult

class MethodAddressSpace(val device: SnmpAgentDevice, composite: AddressSpaceComposite) :
    DeviceContextManagedAddressSpaceFragment(device.context.deviceContext, composite) {

    val root = "Methods"

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
        val folder =
            UaFolderNode(
                nodeContext,
                nodeId(root),
                qualifiedName(root),
                LocalizedText.english(root),
            )
        nodeManager.addNode(folder)

        folder.addReference(
            Reference(
                folder.nodeId,
                NodeIds.Organizes,
                deviceNodeId.expanded(),
                Reference.Direction.INVERSE,
            )
        )

        addMethodNode(
            folder.nodeId,
            "Walk",
            arrayOf(
                Argument(
                    "Root",
                    NodeIds.String,
                    ValueRank.ScalarOrOneDimension.value,
                    null,
                    LocalizedText.english("Root OID to walk."),
                )
            ),
            arrayOf(
                Argument(
                    "Result",
                    OidValueType.TYPE_ID.toNodeId(server.namespaceTable).get(),
                    ValueRank.ScalarOrOneDimension.value,
                    null,
                    LocalizedText.english("Result string will go here."),
                )
            ),
        ) { _, request ->
            val root =
                request.inputArguments?.first()?.value as String?
                    ?: return@addMethodNode CallMethodResult(
                        StatusCode.BAD,
                        arrayOfNulls<StatusCode>(0),
                        arrayOfNulls<DiagnosticInfo>(0),
                        arrayOf(),
                    )

            val results =
                device.walk(listOf(Snmp4jOid(root))).map {
                    ExtensionObject.encode(
                        server.staticEncodingContext,
                        OidValueType(it.oid.numeric, it.value.value.value),
                    )
                }

            val output = Variant(results.toTypedArray())

            CallMethodResult(
                StatusCode.GOOD,
                arrayOfNulls<StatusCode>(0),
                arrayOfNulls<DiagnosticInfo>(0),
                arrayOf(output),
            )
        }
    }

    fun addMethodNode(
        parent: NodeId,
        name: String,
        inputs: Array<Argument>,
        outputs: Array<Argument>,
        handler: MethodInvocationHandler,
    ) {
        UaMethodNode.UaMethodNodeBuilder(nodeContext)
            .run {
                setNodeId(nodeId(name))
                setBrowseName(qualifiedName(name))
                setDisplayName(LocalizedText.english(name))
                addReference(
                    Reference(
                        nodeId,
                        NodeIds.HasComponent,
                        parent.expanded(),
                        Reference.Direction.INVERSE,
                    )
                )
                buildAndAdd()
            }
            .run {
                inputArguments = inputs
                outputArguments = outputs
                invocationHandler = handler
            }
    }
}
