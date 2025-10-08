package com.mussonindustrial.embr.snmp.agents.opc

import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import com.mussonindustrial.embr.snmp.opc.DelegatingSubscriptionModel
import com.mussonindustrial.embr.snmp.requests.OidReadRequest
import com.mussonindustrial.embr.snmp.requests.OidReadResult
import com.mussonindustrial.embr.snmp.requests.OidWriteRequest
import com.mussonindustrial.embr.snmp.requests.OidWriteResult
import com.mussonindustrial.embr.snmp.requests.toOidWriteResult
import com.mussonindustrial.embr.snmp.utils.isOid
import com.mussonindustrial.embr.snmp.utils.toVariable
import kotlin.jvm.optionals.getOrNull
import org.eclipse.milo.opcua.sdk.core.AccessLevel
import org.eclipse.milo.opcua.sdk.core.ValueRank
import org.eclipse.milo.opcua.sdk.server.AddressSpace
import org.eclipse.milo.opcua.sdk.server.AddressSpaceFilter
import org.eclipse.milo.opcua.sdk.server.AddressSpaceFragment
import org.eclipse.milo.opcua.sdk.server.Lifecycle
import org.eclipse.milo.opcua.sdk.server.SimpleAddressSpaceFilter
import org.eclipse.milo.opcua.sdk.server.items.DataItem
import org.eclipse.milo.opcua.sdk.server.items.MonitoredItem
import org.eclipse.milo.opcua.stack.core.AttributeId
import org.eclipse.milo.opcua.stack.core.OpcUaDataType
import org.eclipse.milo.opcua.stack.core.StatusCodes
import org.eclipse.milo.opcua.stack.core.UaException
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId
import org.eclipse.milo.opcua.stack.core.types.structured.ViewDescription
import org.eclipse.milo.opcua.stack.core.types.structured.WriteValue
import org.snmp4j.smi.OID
import org.snmp4j.smi.VariableBinding

class OidAddressSpace(val device: SnmpAgentDevice) : AddressSpaceFragment, Lifecycle {

    private val filter = SimpleAddressSpaceFilter.create { it.getPath().isOid() }
    private val subscriptionModel =
        DelegatingSubscriptionModel(device.context.deviceContext.server) {
            read(context, maxAge, timestamps, readValueIds)
        }

    override fun startup() {
        subscriptionModel.startup()
    }

    override fun shutdown() {
        subscriptionModel.shutdown()
    }

    override fun read(
        context: AddressSpace.ReadContext,
        maxAge: Double,
        timestamps: TimestampsToReturn,
        readValueIds: List<ReadValueId>,
    ): List<DataValue> {
        val results = readValueIds.map { ReadRequest(it) }
        val toProcess = results.filter { it.result == null }

        val valueReads =
            toProcess.filter {
                AttributeId.from(it.readValueId.attributeId).get() == AttributeId.Value
            }
        val valueReadResults = device.read(valueReads.map { VariableBinding(it.oid) })
        valueReadResults.zip(valueReads).forEach { (value, result) -> result.result = value }

        val nonValueReads =
            toProcess.filter {
                AttributeId.from(it.readValueId.attributeId).get() != AttributeId.Value
            }
        val nonValueReadResults = readNonValueAttributes(nonValueReads)
        nonValueReadResults.zip(nonValueReads).forEach { (value, result) -> result.result = value }

        return results.map { it.result!!.value }
    }

    fun readNonValueAttributes(results: List<ReadRequest>): List<OidReadResult> {
        return results.map {
            val nodeId = it.readValueId.nodeId
            val attributeId = AttributeId.from(it.readValueId.attributeId).getOrNull()

            try {
                val result =
                    when (attributeId) {
                        AttributeId.NodeId -> nodeId

                        AttributeId.NodeClass -> NodeClass.Variable

                        AttributeId.BrowseName ->
                            device.context.deviceContext.qualifiedName(nodeId.getPath())

                        AttributeId.DisplayName,
                        AttributeId.Description -> LocalizedText.english(nodeId.getPath())

                        AttributeId.WriteMask,
                        AttributeId.UserWriteMask -> UInteger.valueOf(0)

                        AttributeId.DataType -> OpcUaDataType.String.nodeId

                        AttributeId.ValueRank -> ValueRank.Scalar.value

                        AttributeId.ArrayDimensions -> intArrayOf()

                        AttributeId.AccessLevel,
                        AttributeId.UserAccessLevel -> AccessLevel.toValue(AccessLevel.READ_WRITE)

                        AttributeId.Value ->
                            throw UaException(
                                StatusCodes.Bad_InternalError,
                                "attributeId: $attributeId",
                            )

                        else ->
                            throw UaException(
                                StatusCodes.Bad_AttributeIdInvalid,
                                "attributeId: $attributeId",
                            )
                    }!!

                OidReadResult(DataValue(Variant(result)))
            } catch (e: UaException) {
                OidReadResult(DataValue(e.statusCode))
            }
        }
    }

    override fun write(
        context: AddressSpace.WriteContext,
        writeValues: List<WriteValue>,
    ): List<StatusCode?> {
        val results = writeValues.map { WriteRequest(it) }

        results.forEach {
            if (it.writeValue.attributeId == null) {
                it.result = StatusCode(StatusCodes.Bad_AttributeIdInvalid).toOidWriteResult()
            }
            if (it.writeValue.indexRange != null && it.writeValue.indexRange.isNotEmpty()) {
                it.result = StatusCode(StatusCodes.Bad_NotImplemented).toOidWriteResult()
            }
            if (AttributeId.from(it.writeValue.attributeId).getOrNull() != AttributeId.Value) {
                it.result = StatusCode(StatusCodes.Bad_WriteNotSupported).toOidWriteResult()
            }
        }

        val valueWrites = results.filter { it.result == null }
        val valueWriteResults =
            device.write(valueWrites.map { VariableBinding(it.oid, it.value.toVariable()) })
        valueWriteResults.zip(valueWrites).forEach { (value, result) -> result.result = value }

        return results.map { it.result?.statusCode }
    }

    override fun browse(
        context: AddressSpace.BrowseContext,
        view: ViewDescription,
        nodeIds: List<NodeId>,
    ): List<AddressSpace.ReferenceResult> {
        return emptyList()
    }

    override fun gather(
        context: AddressSpace.BrowseContext,
        view: ViewDescription,
        nodeId: NodeId,
    ): AddressSpace.ReferenceResult.ReferenceList {
        return AddressSpace.ReferenceResult.ReferenceList(emptyList())
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

    fun NodeId.getPath(): String {
        return device.stripDeviceName(this)
    }

    inner class ReadRequest(val readValueId: ReadValueId) : OidReadRequest {
        override var result: OidReadResult? = null
        override val oid = OID(readValueId.nodeId.getPath())
    }

    inner class WriteRequest(val writeValue: WriteValue) : OidWriteRequest {
        override var result: OidWriteResult? = null
        override val value: DataValue = writeValue.value
        override val oid = OID(writeValue.nodeId.getPath())
    }
}
