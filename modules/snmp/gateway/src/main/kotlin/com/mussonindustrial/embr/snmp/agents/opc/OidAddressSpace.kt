package com.mussonindustrial.embr.snmp.agents.opc

import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import com.mussonindustrial.embr.snmp.agents.model.ObjectModel
import com.mussonindustrial.embr.snmp.model.OidValue
import com.mussonindustrial.embr.snmp.model.Snmp4jOid
import com.mussonindustrial.embr.snmp.opc.DeviceContextManagedAddressSpaceFragment
import com.mussonindustrial.embr.snmp.utils.isOid
import kotlin.jvm.optionals.getOrNull
import org.eclipse.milo.opcua.sdk.core.AccessLevel
import org.eclipse.milo.opcua.sdk.core.Reference
import org.eclipse.milo.opcua.sdk.core.ValueRank
import org.eclipse.milo.opcua.sdk.server.AddressSpace
import org.eclipse.milo.opcua.sdk.server.AddressSpaceComposite
import org.eclipse.milo.opcua.sdk.server.AddressSpaceFilter
import org.eclipse.milo.opcua.sdk.server.SimpleAddressSpaceFilter
import org.eclipse.milo.opcua.stack.core.*
import org.eclipse.milo.opcua.stack.core.types.builtin.*
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId
import org.eclipse.milo.opcua.stack.core.types.structured.ViewDescription
import org.eclipse.milo.opcua.stack.core.types.structured.WriteValue

class OidAddressSpace(val device: SnmpAgentDevice, composite: AddressSpaceComposite) :
    DeviceContextManagedAddressSpaceFragment(device.context.deviceContext, composite) {

    private val filter = SimpleAddressSpaceFilter.create { it.getPath().isOid() }
    private val model = device.model

    override fun read(
        context: AddressSpace.ReadContext,
        maxAge: Double,
        timestamps: TimestampsToReturn,
        readValueIds: List<ReadValueId>,
    ): List<DataValue> {
        val requests = readValueIds.map { ReadRequest(it) }

        val valueReads =
            requests.filter {
                AttributeId.from(it.readValueId.attributeId).get() == AttributeId.Value
            }
        readValueAttributes(valueReads).zip(valueReads).forEach { (value, result) ->
            result.value = value
        }

        val nonValueReads =
            requests.filter {
                AttributeId.from(it.readValueId.attributeId).get() != AttributeId.Value
            }
        readNonValueAttributes(nonValueReads).zip(nonValueReads).forEach { (value, result) ->
            result.value = value
        }

        return requests.map { it.value ?: DataValue(Variant.of(null)) }
    }

    fun readValueAttributes(requests: List<ReadRequest>): List<DataValue> {
        return model.read(requests.map { it.oid }).map { it.value }
    }

    fun readNonValueAttributes(requests: List<ReadRequest>): List<DataValue> {
        val descriptors = model.getDescriptors(requests.map { it.oid })

        return requests.zip(descriptors).map { (request, descriptor) ->
            val nodeId = request.readValueId.nodeId
            val attributeId = AttributeId.from(request.readValueId.attributeId).getOrNull()

            attributeId
                .runCatching { resolveAttributeValue(attributeId, nodeId, descriptor) }
                .fold(
                    onSuccess = { DataValue(Variant(it)) },
                    onFailure = { DataValue((it as UaException).statusCode) },
                )
        }
    }

    @Throws(UaException::class)
    fun resolveAttributeValue(
        attributeId: AttributeId?,
        nodeId: NodeId,
        descriptor: ObjectModel.Descriptor,
    ): Any? {
        return when (attributeId) {
            AttributeId.NodeId -> nodeId

            AttributeId.NodeClass -> NodeClass.Variable

            AttributeId.BrowseName -> device.context.deviceContext.qualifiedName(nodeId.getPath())

            AttributeId.DisplayName -> LocalizedText.english(descriptor.oid.numeric)
            AttributeId.Description -> LocalizedText.english(descriptor.oid.numeric)

            AttributeId.WriteMask,
            AttributeId.UserWriteMask -> UInteger.valueOf(0)

            AttributeId.DataType ->
                when (descriptor) {
                    is ObjectModel.ValueDescriptor -> descriptor.snmpType.uaDataType
                    else -> OpcUaDataType.String.nodeId
                }
            AttributeId.ValueRank ->
                when (descriptor) {
                    is ObjectModel.ValueDescriptor -> ValueRank.Scalar.value
                    else -> ValueRank.Scalar.value
                }
            AttributeId.ArrayDimensions ->
                when (descriptor) {
                    is ObjectModel.ValueDescriptor -> null
                    else -> null
                }

            AttributeId.AccessLevel,
            AttributeId.UserAccessLevel -> AccessLevel.toValue(AccessLevel.READ_WRITE)

            AttributeId.Historizing -> false

            AttributeId.Value ->
                throw UaException(StatusCodes.Bad_InternalError, "attributeId: $attributeId")

            else ->
                throw UaException(StatusCodes.Bad_AttributeIdInvalid, "attributeId: $attributeId")
        }
    }

    override fun write(
        context: AddressSpace.WriteContext,
        writeValues: List<WriteValue>,
    ): List<StatusCode?> {
        val results = writeValues.map { WriteRequest(it) }

        results.forEach {
            if (it.writeValue.attributeId == null) {
                it.value = StatusCode(StatusCodes.Bad_AttributeIdInvalid)
            }
            if (it.writeValue.indexRange != null && it.writeValue.indexRange.isNotEmpty()) {
                it.value = StatusCode(StatusCodes.Bad_NotImplemented)
            }
            if (AttributeId.from(it.writeValue.attributeId).getOrNull() != AttributeId.Value) {
                it.value = StatusCode(StatusCodes.Bad_NotImplemented)
            }
        }

        val valueWrites = results.filter { it.value == null }
        model
            .write(valueWrites.map { it.oid to it.writeValue.value.value.value })
            .zip(valueWrites)
            .forEach { (value, result) -> result.value = value.value }

        return results.map { it.value }
    }

    override fun browse(
        context: AddressSpace.BrowseContext,
        view: ViewDescription,
        nodeIds: List<NodeId>,
    ): List<AddressSpace.ReferenceResult> {
        return nodeIds.map { nodeId ->
            AddressSpace.ReferenceResult.of(
                listOf(
                    Reference(
                        nodeId,
                        NodeIds.Organizes,
                        nodeId("Objects/Numeric").expanded(),
                        Reference.Direction.INVERSE,
                    )
                )
            )
        }
    }

    override fun getFilter(): AddressSpaceFilter {
        return filter
    }

    fun NodeId.getPath(): String {
        return device.stripDeviceName(this)
    }

    inner class ReadRequest(val readValueId: ReadValueId) : OidValue<DataValue?> {
        override val oid = Snmp4jOid(readValueId.nodeId.getPath())
        override var value: DataValue? = null
    }

    inner class WriteRequest(val writeValue: WriteValue) : OidValue<StatusCode?> {
        override val oid = Snmp4jOid(writeValue.nodeId.getPath())
        override var value: StatusCode? = null
    }
}
