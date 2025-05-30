package com.mussonindustrial.embr.snmp.drivers

import com.inductiveautomation.ignition.common.TypeUtilities
import com.mussonindustrial.embr.common.logging.getLogger
import kotlin.jvm.optionals.getOrNull
import org.eclipse.milo.opcua.sdk.core.AccessLevel
import org.eclipse.milo.opcua.sdk.core.ValueRank
import org.eclipse.milo.opcua.sdk.server.Lifecycle
import org.eclipse.milo.opcua.sdk.server.api.*
import org.eclipse.milo.opcua.sdk.server.api.services.AttributeServices
import org.eclipse.milo.opcua.sdk.server.api.services.ViewServices
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel
import org.eclipse.milo.opcua.stack.core.AttributeId
import org.eclipse.milo.opcua.stack.core.BuiltinDataType
import org.eclipse.milo.opcua.stack.core.StatusCodes
import org.eclipse.milo.opcua.stack.core.UaException
import org.eclipse.milo.opcua.stack.core.types.builtin.*
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId
import org.eclipse.milo.opcua.stack.core.types.structured.ViewDescription
import org.eclipse.milo.opcua.stack.core.types.structured.WriteValue
import org.snmp4j.PDU
import org.snmp4j.Snmp
import org.snmp4j.Target
import org.snmp4j.smi.*

class OidAddressSpace(
    private val device: SnmpDevice,
    private val snmp: Snmp,
    private val target: Target<Address>,
) : AddressSpaceFragment, Lifecycle {

    private val logger = this.getLogger()
    private val filter =
        SimpleAddressSpaceFilter.create {
            return@create it.toPath().startsWith(".")
        }
    private val subscriptionModel = SubscriptionModel(device.deviceContext.getServer(), this)
    private val knownNodes = HashMap<NodeId, Variable>()

    override fun startup() {
        subscriptionModel.startup()
        device.register(this)
    }

    override fun shutdown() {
        subscriptionModel.shutdown()
        device.unregister(this)
    }

    override fun getFilter(): AddressSpaceFilter {
        return filter
    }

    override fun browse(
        context: ViewServices.BrowseContext,
        viewDescription: ViewDescription,
        nodeId: NodeId,
    ) {
        context.success(emptyList())
    }

    override fun getReferences(
        context: ViewServices.BrowseContext,
        viewDescription: ViewDescription,
        nodeId: NodeId,
    ) {
        context.success(emptyList())
    }

    private fun NodeId.toPath(): String {
        val id = this.identifier.toString()
        val name = "[${device.getName()}]"
        return id.substring(id.indexOf(name) + name.length)
    }

    inner class ReadResult(val readValueId: ReadValueId) {
        var value: DataValue? = null
        val oid = let {
            try {
                OID(readValueId.nodeId.toPath())
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun read(
        context: AttributeServices.ReadContext,
        maxAge: Double,
        timestamps: TimestampsToReturn,
        readValueIds: List<ReadValueId>,
    ) {

        val results = readValueIds.map { ReadResult(it) }

        results.forEach {
            if (it.readValueId.attributeId == null) {
                it.value = DataValue(StatusCodes.Bad_AttributeIdInvalid)
            }
            if (it.readValueId.indexRange != null && it.readValueId.indexRange.isNotEmpty()) {
                it.value = DataValue(StatusCodes.Bad_NotImplemented)
            }
        }

        val toProcess = results.filter { it.value == null }

        val valueReads =
            toProcess.filter {
                AttributeId.from(it.readValueId.attributeId).get() == AttributeId.Value
            }
        snmpGet(valueReads)

        val nonValueReads =
            toProcess.filter {
                AttributeId.from(it.readValueId.attributeId).get() != AttributeId.Value
            }
        readNonValueAttributes(nonValueReads)

        context.success(results.map { it.value })
    }

    private fun snmpGet(results: List<ReadResult>) {
        val remaining =
            results
                .mapNotNull {
                    try {
                        it.oid to it
                    } catch (e: Exception) {
                        it.value = DataValue(StatusCodes.Bad_NodeIdInvalid)
                        null
                    }
                }
                .groupBy({ it.first }, { it.second })
                .toMutableMap()

        while (remaining.isNotEmpty()) {
            val pdu =
                PDU().apply {
                    type = PDU.GET
                    remaining.keys.forEach { oid -> add(VariableBinding(oid)) }
                }

            try {
                val response = snmp.send(pdu, target).response
                if (response == null) {
                    logger.warn("GET failed: no response.")
                    remaining.values.flatten().forEach {
                        it.value = DataValue(StatusCodes.Bad_CommunicationError)
                    }
                    break
                }

                if (response.errorStatus != 0) {
                    val errorIdx = response.errorIndex
                    if (errorIdx in 1..pdu.size()) {
                        val badOid = pdu.get(errorIdx - 1).oid
                        logger.debug(
                            "GET failed at OID: {} (index {}), removing and retrying...",
                            badOid,
                            errorIdx,
                        )
                        val failedResults = remaining.remove(badOid)
                        failedResults?.forEach {
                            it.value = DataValue(StatusCodes.Bad_NodeIdUnknown)
                        }
                    } else {
                        logger.warn("GET failed with invalid errorIndex: $errorIdx")
                        remaining.values.flatten().forEach {
                            it.value = DataValue(StatusCodes.Bad_CommunicationError)
                        }
                        break
                    }
                } else {
                    logger.trace("GET successful: {}", response.variableBindings)
                    response.variableBindings.forEach { binding ->
                        remaining[binding.oid]?.forEach {
                            knownNodes[it.readValueId.nodeId] = binding.variable

                            it.value =
                                when (binding.variable) {
                                    is TimeTicks -> DataValue(Variant(binding.variable.toLong()))
                                    else -> DataValue(Variant(binding.variable.toString()))
                                }
                        }
                    }
                    break // All succeeded, exit loop
                }
            } catch (e: Exception) {
                logger.debug("GET failed with exception", e)
                remaining.values.flatten().forEach {
                    it.value = DataValue(StatusCodes.Bad_CommunicationError)
                }
                break
            }
        }
    }

    private fun readNonValueAttributes(results: List<ReadResult>) {
        results.forEach {
            val nodeId = it.readValueId.nodeId
            val attributeId = AttributeId.from(it.readValueId.attributeId).getOrNull()

            try {
                val result =
                    when (attributeId) {
                        AttributeId.NodeId -> nodeId

                        AttributeId.NodeClass -> NodeClass.Variable

                        AttributeId.BrowseName ->
                            device.deviceContext.qualifiedName(nodeId.toPath())

                        AttributeId.DisplayName,
                        AttributeId.Description -> LocalizedText.english(nodeId.toPath())

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

                it.value = DataValue(Variant(result))
            } catch (e: UaException) {
                it.value = DataValue(e.statusCode)
            }
        }
    }

    inner class WriteResult(val writeValue: WriteValue) {
        var statusCode: StatusCode? = null
        val oid = let {
            try {
                OID(writeValue.nodeId.toPath())
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun write(
        context: AttributeServices.WriteContext,
        writeValues: MutableList<WriteValue>,
    ) {
        val results = writeValues.map { WriteResult(it) }

        results.forEach {
            if (it.writeValue.attributeId == null) {
                it.statusCode = StatusCode(StatusCodes.Bad_AttributeIdInvalid)
            }
            if (it.writeValue.indexRange != null && it.writeValue.indexRange.isNotEmpty()) {
                it.statusCode = StatusCode(StatusCodes.Bad_NotImplemented)
            }
            if (AttributeId.from(it.writeValue.attributeId).getOrNull() != AttributeId.Value) {
                it.statusCode = StatusCode(StatusCodes.Bad_WriteNotSupported)
            }
        }

        val toProcess = results.filter { it.statusCode == null }

        snmpSet(toProcess)

        context.success(results.map { it.statusCode })
    }

    private fun snmpSet(results: List<WriteResult>) {
        results.forEach {
            val value = it.writeValue.value.value
            val variable: Variable?

            val known = knownNodes[it.writeValue.nodeId]
            if (known != null) {
                variable =
                    when (known) {
                        is OctetString -> OctetString(TypeUtilities.toString(value.value))
                        is Integer32 -> Integer32(TypeUtilities.toInteger(value.value))
                        is Gauge32 -> Gauge32(TypeUtilities.toLong(value.value))
                        is Counter32 -> Counter32(TypeUtilities.toLong(value.value))
                        is UnsignedInteger32 ->
                            UnsignedInteger32(TypeUtilities.toInteger(value.value))
                        is Counter64 -> Counter64(TypeUtilities.toLong(value.value))
                        is IpAddress -> IpAddress(TypeUtilities.toString(value.value))
                        is OID -> OID(TypeUtilities.toString(value.value))
                        else -> null
                    }
            } else {
                val dataType = BuiltinDataType.fromNodeId(value.dataType.get())
                variable =
                    when (dataType) {
                        BuiltinDataType.String -> OctetString(TypeUtilities.toString(value.value))

                        BuiltinDataType.Int16,
                        BuiltinDataType.Int32 -> Integer32(TypeUtilities.toInteger(value.value))

                        BuiltinDataType.UInt16,
                        BuiltinDataType.UInt32 ->
                            UnsignedInteger32(TypeUtilities.toInteger(value.value))
                        else -> null
                    }
            }

            if (variable == null) {
                it.statusCode = StatusCode(StatusCodes.Bad_WriteNotSupported)
                return@forEach
            }

            val pdu =
                PDU().apply {
                    type = PDU.SET
                    add(VariableBinding(it.oid, variable))
                }

            try {
                val response = snmp.send(pdu, target).response
                if (response == null) {
                    logger.warn("SET failed: no response.")
                    it.statusCode = StatusCode(StatusCodes.Bad_CommunicationError)
                    return@forEach
                }

                if (response.errorStatus == 0) {
                    it.statusCode = StatusCode.GOOD
                } else {
                    it.statusCode = StatusCode.BAD
                }
            } catch (e: Exception) {
                logger.debug("GET failed with exception", e)
                it.statusCode = StatusCode(StatusCodes.Bad_CommunicationError)
                return@forEach
            }
        }
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
