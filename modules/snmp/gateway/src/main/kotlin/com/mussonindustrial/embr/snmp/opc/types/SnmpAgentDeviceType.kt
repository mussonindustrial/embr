package com.mussonindustrial.embr.snmp.opc.types

import org.eclipse.milo.opcua.sdk.core.Reference
import org.eclipse.milo.opcua.sdk.server.ManagedNamespace
import org.eclipse.milo.opcua.sdk.server.nodes.UaObjectTypeNode
import org.eclipse.milo.opcua.stack.core.NodeIds
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint

object SnmpAgentDeviceType {

    const val BROWSE_NAME = "SnmpAgentDeviceType"

    fun nodeId(namespace: ManagedNamespace): NodeId =
        NodeId(namespace.namespaceIndex, "ObjectType.$BROWSE_NAME")

    fun register(namespace: ManagedNamespace) {
        UaObjectTypeNode(
                namespace.nodeContext,
                nodeId(namespace),
                QualifiedName(namespace.namespaceIndex, BROWSE_NAME),
                LocalizedText.english(BROWSE_NAME),
                LocalizedText.english(""),
                uint(0),
                uint(0),
                false,
            )
            .apply {
                namespace.nodeManager.addNode(this)
                addReference(
                    Reference(
                        nodeId(namespace),
                        NodeIds.HasSubtype,
                        NodeIds.BaseObjectType.expanded(),
                        Reference.Direction.INVERSE,
                    )
                )
            }
    }
}
