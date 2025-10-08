package com.mussonindustrial.embr.snmp.opc

import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import org.eclipse.milo.opcua.sdk.server.AddressSpaceComposite
import org.eclipse.milo.opcua.sdk.server.AddressSpaceFilter
import org.eclipse.milo.opcua.sdk.server.Lifecycle
import org.eclipse.milo.opcua.sdk.server.ManagedAddressSpaceFragmentWithLifecycle
import org.eclipse.milo.opcua.sdk.server.OpcUaServer
import org.eclipse.milo.opcua.sdk.server.SimpleAddressSpaceFilter
import org.eclipse.milo.opcua.sdk.server.items.DataItem
import org.eclipse.milo.opcua.sdk.server.items.MonitoredItem
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName

open class DeviceContextManagedAddressSpaceFragment(
    val deviceContext: DeviceContext,
    composite: AddressSpaceComposite,
) :
    ManagedAddressSpaceFragmentWithLifecycle(deviceContext.server, composite),
    Lifecycle,
    DeviceContext by deviceContext {

    private val filter = SimpleAddressSpaceFilter.create { nodeManager.containsNode(it) }
    private val subscriptionModel = SubscriptionModel(deviceContext.server, this)

    init {
        lifecycleManager.addLifecycle(subscriptionModel)
    }

    override fun getFilter(): AddressSpaceFilter {
        return filter
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

    override fun getDeviceNodeId(): NodeId {
        return super.getDeviceNodeId()
    }

    override fun getServer(): OpcUaServer {
        return super.getServer()
    }

    override fun nodeId(id: Any): NodeId {
        return super.nodeId(id)
    }

    override fun qualifiedName(name: String): QualifiedName {
        return super.qualifiedName(name)
    }
}
