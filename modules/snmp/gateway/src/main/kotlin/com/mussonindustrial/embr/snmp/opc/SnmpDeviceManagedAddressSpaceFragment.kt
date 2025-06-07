package com.mussonindustrial.embr.snmp.opc

import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.mussonindustrial.embr.snmp.devices.SnmpDeviceImpl
import org.eclipse.milo.opcua.sdk.server.Lifecycle
import org.eclipse.milo.opcua.sdk.server.OpcUaServer
import org.eclipse.milo.opcua.sdk.server.api.AddressSpaceFilter
import org.eclipse.milo.opcua.sdk.server.api.DataItem
import org.eclipse.milo.opcua.sdk.server.api.ManagedAddressSpaceFragmentWithLifecycle
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem
import org.eclipse.milo.opcua.sdk.server.api.SimpleAddressSpaceFilter
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel

open class SnmpDeviceManagedAddressSpaceFragment(val device: SnmpDeviceImpl<*>) :
    ManagedAddressSpaceFragmentWithLifecycle(device.context.deviceContext.getServer(), device),
    Lifecycle,
    DeviceContext by device.context.deviceContext {

    private val filter = SimpleAddressSpaceFilter.create { nodeManager.containsNode(it) }
    private val subscriptionModel =
        SubscriptionModel(device.context.deviceContext.getServer(), this)

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

    override fun getServer(): OpcUaServer {
        return super.getServer()
    }
}
