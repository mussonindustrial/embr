package com.mussonindustrial.embr.snmp.opc

import com.mussonindustrial.embr.snmp.agents.opc.types.OidValueType
import org.eclipse.milo.opcua.sdk.server.ManagedNamespaceWithLifecycle
import org.eclipse.milo.opcua.sdk.server.OpcUaServer
import org.eclipse.milo.opcua.sdk.server.items.DataItem
import org.eclipse.milo.opcua.sdk.server.items.MonitoredItem
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId

class SnmpNamespace(server: OpcUaServer) : ManagedNamespaceWithLifecycle(server, NAMESPACE_URI) {

    companion object {
        const val NAMESPACE_URI = "urn:mussonindustrial:embr:snmp"
        lateinit var instance: SnmpNamespace
    }

    private val subscriptionModel = SubscriptionModel(server, this)

    init {
        instance = this
        lifecycleManager.addLifecycle(subscriptionModel)
        lifecycleManager.addStartupTask { OidValueType.register(this) }
    }

    fun nodeId(id: Any): NodeId {
        return ExpandedNodeId(
                ExpandedNodeId.ServerReference.ServerIndex.LOCAL,
                ExpandedNodeId.NamespaceReference.NamespaceUri(NAMESPACE_URI),
                id,
            )
            .toNodeId(server.namespaceTable)
            .get()
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
