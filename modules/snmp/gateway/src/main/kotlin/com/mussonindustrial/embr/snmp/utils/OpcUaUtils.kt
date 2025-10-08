package com.mussonindustrial.embr.snmp.utils

import org.eclipse.milo.opcua.sdk.server.UaNodeManager

fun UaNodeManager.removeAllNodes() {
    this.nodes.forEach { removeNode(it) }
}
