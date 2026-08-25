package com.mussonindustrial.embr.snmp.agents.opc

import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import com.mussonindustrial.embr.snmp.opc.DeviceContextManagedAddressSpaceFragment
import com.mussonindustrial.embr.snmp.requests.OidReadRequest
import com.mussonindustrial.embr.snmp.requests.OidReadResult
import com.mussonindustrial.embr.snmp.requests.OidWriteRequest
import com.mussonindustrial.embr.snmp.requests.OidWriteResult
import com.mussonindustrial.embr.snmp.requests.toOidReadResult
import com.mussonindustrial.embr.snmp.requests.toOidWriteResult
import com.mussonindustrial.embr.snmp.utils.isOid
import com.mussonindustrial.embr.snmp.utils.toVariable
import org.eclipse.milo.opcua.sdk.core.AccessLevel
import org.eclipse.milo.opcua.sdk.core.ValueRank
import org.eclipse.milo.opcua.sdk.server.api.AddressSpaceComposite
import org.eclipse.milo.opcua.sdk.server.api.AddressSpaceFilter
import org.eclipse.milo.opcua.sdk.server.api.SimpleAddressSpaceFilter
import org.eclipse.milo.opcua.sdk.server.api.services.AttributeServices
import org.eclipse.milo.opcua.sdk.server.api.services.ViewServices
import org.eclipse.milo.opcua.stack.core.AttributeId
import org.eclipse.milo.opcua.stack.core.BuiltinDataType
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

class OidAddressSpace(val device: SnmpAgentDevice, composite: AddressSpaceComposite) :
    DeviceContextManagedAddressSpaceFragment(device.context.deviceContext, composite) {

    private val filter = SimpleAddressSpaceFilter.create { it.getPath().isOid() }

    override fun read(
        context: AttributeServices.ReadContext,
        maxAge: Double,
        timestamps: TimestampsToReturn,
        readValueIds: List<ReadValueId>,
    ) {
        val results = readValueIds.map { ReadRequest(it) }
        val toProcess = results.filter { it.result == null }

        val valueReads = toProcess.filter {
            AttributeId.from(it.readValueId.attributeId).get() == AttributeId.Value
        }
        val valueReadResults = device.read(valueReads.map { VariableBinding(it.oid) })
        valueReadResults.zip(valueReads).forEach { (value, result) -> result.result = value }

        val nonValueReads = toProcess.filter {
            AttributeId.from(it.readValueId.attributeId).get() != AttributeId.Value
        }
        val nonValueReadResults = readNonValueAttributes(nonValueReads)
        nonValueReadResults.zip(nonValueReads).forEach { (value, result) -> result.result = value }

        context.success(results.map { it.result?.value })
    }

    fun readNonValueAttributes(results: List<ReadRequest>): List<OidReadResult> {
        return results.map {
            val nodeId = it.readValueId.nodeId
            val attributeId = AttributeId.from(it.readValueId.attributeId).orElse(null)

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

                        AttributeId.DataType -> BuiltinDataType.String.nodeId

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

                it.oid.toOidReadResult(DataValue(Variant(result)))
            } catch (e: UaException) {
                it.oid.toOidReadResult(DataValue(e.statusCode))
            }
        }
    }

    override fun write(context: AttributeServices.WriteContext, writeValues: List<WriteValue>) {
        val results = writeValues.map { WriteRequest(it) }

        results.forEach {
            if (it.writeValue.attributeId == null) {
                it.result = it.oid.toOidWriteResult(StatusCode(StatusCodes.Bad_AttributeIdInvalid))
            }
            if (it.writeValue.indexRange != null && it.writeValue.indexRange.isNotEmpty()) {
                it.result = it.oid.toOidWriteResult(StatusCode(StatusCodes.Bad_NotImplemented))
            }
            if (
                AttributeId.from(it.writeValue.attributeId).orElseGet { null } != AttributeId.Value
            ) {
                it.result = it.oid.toOidWriteResult(StatusCode(StatusCodes.Bad_NotImplemented))
            }
        }

        val valueWrites = results.filter { it.result == null }
        val valueWriteResults =
            device.write(valueWrites.map { VariableBinding(it.oid, it.value.toVariable()) })
        valueWriteResults.zip(valueWrites).forEach { (value, result) -> result.result = value }

        context.success(results.map { it.result?.statusCode })
    }

    override fun browse(
        context: ViewServices.BrowseContext,
        view: ViewDescription,
        nodeId: NodeId,
    ) {
        context.success(emptyList())
    }

    override fun getReferences(
        context: ViewServices.BrowseContext,
        view: ViewDescription,
        nodeId: NodeId,
    ) {
        context.success(emptyList())
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
