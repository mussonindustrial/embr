package com.mussonindustrial.embr.snmp.agents.opc.types

import com.mussonindustrial.embr.snmp.opc.SnmpNamespace
import org.eclipse.milo.opcua.sdk.core.Reference
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode
import org.eclipse.milo.opcua.sdk.server.nodes.UaObjectTypeNode
import org.eclipse.milo.opcua.stack.core.NodeIds
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned
import org.eclipse.milo.opcua.stack.core.types.structured.AccessRestrictionType

object SnmpAgentDeviceType {

    const val BROWSE_NAME = "SnmpAgentDeviceType"

    private fun method(nodeId: NodeId, browseName: String, parent: NodeId) =
        UaMethodNode(
                SnmpNamespace.Companion.nodeContext,
                nodeId,
                SnmpNamespace.Companion.qualifiedName(browseName),
                LocalizedText.english(browseName),
                LocalizedText.english(""),
                Unsigned.uint(0),
                Unsigned.uint(0),
                true,
                true,
            )
            .apply {
                accessRestrictions = AccessRestrictionType.of()
                SnmpNamespace.Companion.nodeManager.addNode(this)
                addReference(
                    Reference(
                        nodeId,
                        NodeIds.HasComponent,
                        parent.expanded(),
                        Reference.Direction.INVERSE,
                    )
                )
                addReference(
                    Reference(
                        nodeId,
                        NodeIds.HasModellingRule,
                        NodeIds.ModellingRule_Mandatory.expanded(),
                        Reference.Direction.FORWARD,
                    )
                )
            }

    fun register() {
        val type =
            UaObjectTypeNode(
                    SnmpNamespace.Companion.nodeContext,
                    SnmpNamespace.Companion.NodesIds.SnmpAgentDeviceType,
                    SnmpNamespace.Companion.qualifiedName(BROWSE_NAME),
                    LocalizedText.english(BROWSE_NAME),
                    LocalizedText.english(""),
                    Unsigned.uint(0),
                    Unsigned.uint(0),
                    false,
                )
                .apply {
                    accessRestrictions = AccessRestrictionType.of()
                    SnmpNamespace.Companion.nodeManager.addNode(this)
                    addReference(
                        Reference(
                            nodeId,
                            NodeIds.HasSubtype,
                            NodeIds.BaseObjectType.expanded(),
                            Reference.Direction.INVERSE,
                        )
                    )
                }

        method(SnmpNamespace.Companion.NodesIds.SnmpAgentDeviceType_Walk, "Walk", type.nodeId)
        method(
            SnmpNamespace.Companion.NodesIds.SnmpAgentDeviceType_ReadTable,
            "ReadTable",
            type.nodeId,
        )
    }
}
