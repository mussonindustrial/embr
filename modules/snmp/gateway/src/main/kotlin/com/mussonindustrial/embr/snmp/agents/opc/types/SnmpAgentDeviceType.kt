package com.mussonindustrial.embr.snmp.agents.opc.types

import com.mussonindustrial.embr.snmp.opc.SnmpNamespace
import com.mussonindustrial.embr.snmp.utils.addComponentOf
import com.mussonindustrial.embr.snmp.utils.addModellingRule
import com.mussonindustrial.embr.snmp.utils.addNode
import com.mussonindustrial.embr.snmp.utils.addOrganizedBy
import com.mussonindustrial.embr.snmp.utils.addSubtypeOf
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode
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
                SnmpNamespace.nodeContext,
                nodeId,
                SnmpNamespace.qualifiedName(browseName),
                LocalizedText.english(browseName),
                LocalizedText.english(""),
                Unsigned.uint(0),
                Unsigned.uint(0),
                true,
                true,
            )
            .apply {
                addNode(SnmpNamespace.nodeManager)
                addComponentOf(parent.expanded())
                addModellingRule(NodeIds.ModellingRule_Mandatory.expanded())

                accessRestrictions = AccessRestrictionType.of()
            }

    fun register() {
        val type =
            UaObjectTypeNode(
                    SnmpNamespace.nodeContext,
                    SnmpNamespace.NodesIds.SnmpAgentDeviceType,
                    SnmpNamespace.qualifiedName(BROWSE_NAME),
                    LocalizedText.english(BROWSE_NAME),
                    LocalizedText.english(""),
                    Unsigned.uint(0),
                    Unsigned.uint(0),
                    false,
                )
                .apply {
                    addNode(SnmpNamespace.nodeManager)
                    addSubtypeOf(NodeIds.BaseObjectType.expanded())

                    accessRestrictions = AccessRestrictionType.of()
                }

        UaFolderNode(
                SnmpNamespace.nodeContext,
                SnmpNamespace.nodeId("${BROWSE_NAME}.Objects"),
                SnmpNamespace.qualifiedName("Objects"),
                LocalizedText.english(""),
            )
            .apply {
                addNode(SnmpNamespace.nodeManager)
                addOrganizedBy(type.nodeId.expanded())

                accessRestrictions = AccessRestrictionType.of()
            }

        method(SnmpNamespace.Companion.NodesIds.SnmpAgentDeviceType_Walk, "Walk", type.nodeId)
        method(
            SnmpNamespace.Companion.NodesIds.SnmpAgentDeviceType_ReadTable,
            "ReadTable",
            type.nodeId,
        )
    }
}
