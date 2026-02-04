package com.mussonindustrial.embr.snmp.opc

import com.mussonindustrial.embr.snmp.agents.opc.types.SnmpAgentDeviceType
import com.mussonindustrial.embr.snmp.opc.types.OidValueType
import com.mussonindustrial.embr.snmp.opc.types.SnmpDataType
import org.eclipse.milo.opcua.sdk.server.ManagedNamespaceWithLifecycle
import org.eclipse.milo.opcua.sdk.server.OpcUaServer
import org.eclipse.milo.opcua.sdk.server.UaNodeManager
import org.eclipse.milo.opcua.sdk.server.items.DataItem
import org.eclipse.milo.opcua.sdk.server.items.MonitoredItem
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort

class SnmpNamespace(server: OpcUaServer) : ManagedNamespaceWithLifecycle(server, NAMESPACE_URI) {

    companion object {
        const val NAMESPACE_URI = "urn:mussonindustrial:embr:snmp"
        lateinit var instance: SnmpNamespace

        fun nodeId(id: Long): NodeId =
            ExpandedNodeId.of(NAMESPACE_URI, id)
                .toNodeIdOrThrow(instance.nodeContext.namespaceTable)

        fun nodeId(id: String): NodeId =
            ExpandedNodeId.of(NAMESPACE_URI, id)
                .toNodeIdOrThrow(instance.nodeContext.namespaceTable)

        fun qualifiedName(name: String): QualifiedName =
            QualifiedName(instance.namespaceIndex, name)

        val nodeContext: UaNodeContext
            get() = instance.nodeContext

        val nodeManager: UaNodeManager
            get() = instance.nodeManager

        val namespaceIndex: UShort
            get() = instance.namespaceIndex

        val NodesIds by lazy { SnmpNamespaceNodeIds() }
    }

    private val subscriptionModel = SubscriptionModel(server, this)

    init {
        instance = this
        lifecycleManager.addLifecycle(subscriptionModel)
        lifecycleManager.addStartupTask { registerTypes() }
    }

    fun registerTypes() {
        SnmpDataType.registerAll()
        SnmpAgentDeviceType.register()
        OidValueType.register()
    }

    override fun onDataItemsCreated(items: List<DataItem>) {
        subscriptionModel.onDataItemsCreated(items)
    }

    override fun onDataItemsModified(items: List<DataItem>) {
        subscriptionModel.onDataItemsModified(items)
    }

    override fun onDataItemsDeleted(items: List<DataItem>) {
        subscriptionModel.onDataItemsDeleted(items)
    }

    override fun onMonitoringModeChanged(items: List<MonitoredItem>) {
        subscriptionModel.onMonitoringModeChanged(items)
    }
}
