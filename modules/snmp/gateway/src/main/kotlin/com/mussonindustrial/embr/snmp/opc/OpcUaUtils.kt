package com.mussonindustrial.embr.snmp.opc

import com.inductiveautomation.ignition.common.model.values.QualityCode
import org.eclipse.milo.opcua.sdk.server.UaNodeManager
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
