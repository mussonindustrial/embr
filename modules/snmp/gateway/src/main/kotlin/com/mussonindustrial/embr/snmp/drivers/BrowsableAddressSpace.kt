package com.mussonindustrial.embr.snmp.drivers

import com.mussonindustrial.embr.common.logging.getLogger
import org.eclipse.milo.opcua.sdk.core.Reference
import org.eclipse.milo.opcua.sdk.server.OpcUaServer
import org.eclipse.milo.opcua.sdk.server.api.*
import org.eclipse.milo.opcua.sdk.server.api.services.AttributeServices
import org.eclipse.milo.opcua.sdk.server.nodes.AttributeContext
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel
import org.eclipse.milo.opcua.stack.core.Identifiers
import org.eclipse.milo.opcua.stack.core.StatusCodes
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId

class BrowsableAddressSpace(server: OpcUaServer, private val device: SnmpDevice) :
    ManagedAddressSpaceFragmentWithLifecycle(server, device) {

    private val logger = this.getLogger()
    private val filter =
        SimpleAddressSpaceFilter.create { nodeId: NodeId ->
            logger.debug("filtering: {}", nodeId)
            return@create nodeManager.containsNode(nodeId)
        }
    private val subscriptionModel = SubscriptionModel(device.deviceContext.getServer(), this)

    init {
        lifecycleManager.addLifecycle(subscriptionModel)
        lifecycleManager.addStartupTask(this::addNodes)
    }

    fun addNodes() {
        val deviceNode =
            UaFolderNode(
                nodeContext,
                device.deviceContext.nodeId(""),
                device.deviceContext.qualifiedName("[${device.deviceContext.getName()}]"),
                LocalizedText("[${device.deviceContext.getName()}]"),
            )
        nodeManager.addNode(deviceNode)

        deviceNode.addReference(
            Reference(
                deviceNode.nodeId,
                Identifiers.Organizes,
                device.deviceContext.getRootNodeId().expanded(),
                Reference.Direction.INVERSE,
            )
        )

        onDataItemsCreated(
            device.deviceContext.getSubscriptionModel().getDataItems(device.getName())
        )
    }

    override fun read(
        context: AttributeServices.ReadContext,
        maxAge: Double,
        timestamps: TimestampsToReturn,
        readValueIds: MutableList<ReadValueId>,
    ) {
        val results = readValueIds.map { ReadResult(it) }
        results.forEach {
            val node = nodeManager.get(it.readValueId.nodeId)
            if (node == null) {
                it.value = DataValue(StatusCodes.Bad_NodeIdUnknown)
            } else {
                it.value =
                    node.readAttribute(
                        AttributeContext(context),
                        it.readValueId.attributeId,
                        timestamps,
                        it.readValueId.indexRange,
                        it.readValueId.dataEncoding,
                    )
            }
        }

        context.success(results.map { it.value })
    }

    data class ReadResult(val readValueId: ReadValueId) {
        var value: DataValue = DataValue(StatusCodes.Bad_NoData)
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

    override fun getFilter(): AddressSpaceFilter {
        return filter
    }

    private fun NodeId.toSnmpPath(): String {
        val id = this.identifier.toString()
        val name = "[${device.getName()}]"
        return id.substring(id.indexOf(name) + name.length)
    }
}
