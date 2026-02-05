package com.mussonindustrial.embr.snmp.utils

import com.inductiveautomation.ignition.common.model.values.QualityCode
import org.eclipse.milo.opcua.sdk.core.Reference
import org.eclipse.milo.opcua.sdk.server.NodeManager
import org.eclipse.milo.opcua.sdk.server.UaNodeManager
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode
import org.eclipse.milo.opcua.stack.core.NodeIds
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode

fun UaNodeManager.removeAllNodes() {
    this.nodes.forEach { removeNode(it) }
}

fun StatusCode.toQualityCode(): QualityCode {
    return if (this.isGood) {
        QualityCode.Good
    } else {
        QualityCode.Bad
    }
}

fun UaNode.addReference(
    sourceNodeId: NodeId,
    referenceTypeId: NodeId,
    targetNodeId: ExpandedNodeId,
    direction: Reference.Direction,
) {
    addReference(Reference(sourceNodeId, referenceTypeId, targetNodeId, direction))
}

fun UaNode.addNode(nodeManager: NodeManager<UaNode>) {
    nodeManager.addNode(this)
}

fun NodeId.componentOf(targetNodeId: ExpandedNodeId) =
    Reference(this, NodeIds.HasComponent, targetNodeId, Reference.Direction.INVERSE)

fun UaNode.addComponentOf(targetNodeId: ExpandedNodeId) {
    addReference(nodeId.componentOf(targetNodeId))
}

fun NodeId.hasEncoding(targetNodeId: ExpandedNodeId) =
    Reference(this, NodeIds.HasEncoding, targetNodeId, Reference.Direction.FORWARD)

fun UaNode.addHasEncoding(targetNodeId: ExpandedNodeId) {
    addReference(nodeId.hasEncoding(targetNodeId))
}

fun NodeId.modellingRule(targetNodeId: ExpandedNodeId) =
    Reference(this, NodeIds.HasModellingRule, targetNodeId, Reference.Direction.FORWARD)

fun UaNode.addModellingRule(targetNodeId: ExpandedNodeId) {
    addReference(nodeId.modellingRule(targetNodeId))
}

fun NodeId.organizedBy(targetNodeId: ExpandedNodeId) =
    Reference(this, NodeIds.Organizes, targetNodeId, Reference.Direction.INVERSE)

fun UaNode.addOrganizedBy(targetNodeId: ExpandedNodeId) {
    addReference(nodeId.organizedBy(targetNodeId))
}

fun NodeId.organizes(targetNodeId: ExpandedNodeId) =
    Reference(this, NodeIds.Organizes, targetNodeId, Reference.Direction.FORWARD)

fun UaNode.addOrganizes(targetNodeId: ExpandedNodeId) {
    addReference(nodeId.organizes(targetNodeId))
}

fun NodeId.hasProperty(targetNodeId: ExpandedNodeId) =
    Reference(this, NodeIds.HasProperty, targetNodeId, Reference.Direction.FORWARD)

fun UaNode.addHasProperty(targetNodeId: ExpandedNodeId) {
    addReference(nodeId.hasProperty(targetNodeId))
}

fun NodeId.propertyOf(targetNodeId: ExpandedNodeId) =
    Reference(this, NodeIds.HasProperty, targetNodeId, Reference.Direction.INVERSE)

fun UaNode.addPropertyOf(targetNodeId: ExpandedNodeId) {
    addReference(nodeId.propertyOf(targetNodeId))
}

fun NodeId.hasSubtype(targetNodeId: ExpandedNodeId) =
    Reference(this, NodeIds.HasSubtype, targetNodeId, Reference.Direction.FORWARD)

fun UaNode.addHasSubtype(targetNodeId: ExpandedNodeId) {
    addReference(nodeId.hasSubtype(targetNodeId))
}

fun NodeId.subTypeOf(targetNodeId: ExpandedNodeId) =
    Reference(this, NodeIds.HasSubtype, targetNodeId, Reference.Direction.INVERSE)

fun UaNode.addSubtypeOf(targetNodeId: ExpandedNodeId) {
    addReference(nodeId.subTypeOf(targetNodeId))
}

fun NodeId.hasTypeDefinition(targetNodeId: ExpandedNodeId) =
    Reference(this, NodeIds.HasTypeDefinition, targetNodeId, Reference.Direction.FORWARD)

fun UaNode.addHasTypeDefinition(targetNodeId: ExpandedNodeId) {
    addReference(nodeId.hasTypeDefinition(targetNodeId))
}
