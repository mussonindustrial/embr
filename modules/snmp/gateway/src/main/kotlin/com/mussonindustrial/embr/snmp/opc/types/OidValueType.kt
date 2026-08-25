package com.mussonindustrial.embr.snmp.opc.types

import com.google.common.base.MoreObjects
import com.google.common.base.Objects
import com.mussonindustrial.embr.snmp.model.OidValue
import com.mussonindustrial.embr.snmp.opc.SnmpNamespace
import com.mussonindustrial.embr.snmp.opc.addHasEncoding
import com.mussonindustrial.embr.snmp.opc.addHasTypeDefinition
import com.mussonindustrial.embr.snmp.opc.addNode
import com.mussonindustrial.embr.snmp.opc.addSubtypeOf
import org.eclipse.milo.opcua.sdk.core.ValueRanks
import org.eclipse.milo.opcua.sdk.server.nodes.UaDataTypeNode
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext
import org.eclipse.milo.opcua.sdk.server.nodes.UaObjectNode
import org.eclipse.milo.opcua.stack.core.NodeIds
import org.eclipse.milo.opcua.stack.core.UaSerializationException
import org.eclipse.milo.opcua.stack.core.encoding.EncodingContext
import org.eclipse.milo.opcua.stack.core.encoding.GenericDataTypeCodec
import org.eclipse.milo.opcua.stack.core.encoding.UaDecoder
import org.eclipse.milo.opcua.stack.core.encoding.UaEncoder
import org.eclipse.milo.opcua.stack.core.types.UaStructuredType
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned
import org.eclipse.milo.opcua.stack.core.types.enumerated.StructureType
import org.eclipse.milo.opcua.stack.core.types.structured.AccessRestrictionType
import org.eclipse.milo.opcua.stack.core.types.structured.StructureDefinition
import org.eclipse.milo.opcua.stack.core.types.structured.StructureField

class OidValueType(val oid: String, val value: Variant) : UaStructuredType {

    constructor(oid: String, value: Any?) : this(oid, Variant(value))

    constructor(oidValue: OidValue<*>) : this(oidValue.oid.numeric, oidValue.value)

    override fun getTypeId(): ExpandedNodeId {
        return typeNodeId.expanded()
    }

    override fun getBinaryEncodingId(): ExpandedNodeId {
        return binaryEncodingNodeId.expanded()
    }

    override fun getXmlEncodingId(): ExpandedNodeId {
        return ExpandedNodeId.NULL_VALUE
    }

    override fun getJsonEncodingId(): ExpandedNodeId {
        return ExpandedNodeId.NULL_VALUE
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as OidValueType
        return Objects.equal(oid, that.oid) && Objects.equal(value, that.value)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(oid, value)
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this).add("oid", oid).add("value", value).toString()
    }

    class Codec : GenericDataTypeCodec<OidValueType>() {
        override fun getType(): Class<OidValueType> {
            return OidValueType::class.java
        }

        @Throws(UaSerializationException::class)
        override fun decodeType(context: EncodingContext, decoder: UaDecoder): OidValueType {
            val oid = decoder.decodeString("Oid")
            val value = decoder.decodeVariant("Value")
            return OidValueType(oid, value)
        }

        @Throws(UaSerializationException::class)
        override fun encodeType(context: EncodingContext, encoder: UaEncoder, value: OidValueType) {
            encoder.encodeString("Oid", value.oid)
            encoder.encodeVariant("Value", value.value)
        }
    }

    companion object {
        const val BROWSE_NAME = "OidValue"

        private val typeNodeId: NodeId by lazy { SnmpNamespace.NodesIds.OidValue }
        private val binaryEncodingNodeId: NodeId by lazy {
            SnmpNamespace.NodesIds.OidValue_Encoding_DefaultBinary
        }

        fun register(nodeContext: UaNodeContext) {
            UaObjectNode(
                    nodeContext,
                    binaryEncodingNodeId,
                    SnmpNamespace.qualifiedName("Default Binary"),
                    LocalizedText.english("Default Binary"),
                    LocalizedText.NULL_VALUE,
                    Unsigned.uint(0),
                    Unsigned.uint(0),
                )
                .apply {
                    addHasTypeDefinition(NodeIds.DataTypeEncodingType.expanded())
                    accessRestrictions = AccessRestrictionType.of()
                    nodeContext.nodeManager.addNode(this)
                }

            UaDataTypeNode(
                    nodeContext,
                    typeNodeId,
                    SnmpNamespace.qualifiedName(BROWSE_NAME),
                    LocalizedText.english(BROWSE_NAME),
                    LocalizedText.NULL_VALUE,
                    Unsigned.uint(0),
                    Unsigned.uint(0),
                    false,
                )
                .apply {
                    addNode(nodeManager)
                    addSubtypeOf(NodeIds.Structure.expanded())
                    addHasEncoding(binaryEncodingNodeId.expanded())

                    accessRestrictions = AccessRestrictionType.of()
                    dataTypeDefinition =
                        StructureDefinition(
                            binaryEncodingNodeId,
                            NodeIds.Structure,
                            StructureType.Structure,
                            arrayOf(
                                StructureField(
                                    "oid",
                                    LocalizedText.NULL_VALUE,
                                    SnmpNamespace.NodesIds.Oid,
                                    ValueRanks.Scalar,
                                    null,
                                    nodeContext.server.config.limits.maxStringLength,
                                    false,
                                ),
                                StructureField(
                                    "value",
                                    LocalizedText.NULL_VALUE,
                                    NodeIds.BaseDataType,
                                    ValueRanks.Scalar,
                                    null,
                                    Unsigned.uint(0),
                                    false,
                                ),
                            ),
                        )

                    nodeContext.server.staticDataTypeManager.registerType(
                        nodeId,
                        Codec(),
                        binaryEncodingNodeId,
                        null,
                        null,
                    )
                }
        }
    }
}
