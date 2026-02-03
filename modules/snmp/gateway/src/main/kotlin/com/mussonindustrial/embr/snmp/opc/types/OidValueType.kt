package com.mussonindustrial.embr.snmp.opc.types

import com.google.common.base.MoreObjects
import com.google.common.base.Objects
import com.mussonindustrial.embr.snmp.model.OidValue
import com.mussonindustrial.embr.snmp.opc.SnmpNamespace
import org.eclipse.milo.opcua.sdk.core.Reference
import org.eclipse.milo.opcua.sdk.core.ValueRanks
import org.eclipse.milo.opcua.sdk.server.ManagedNamespace
import org.eclipse.milo.opcua.sdk.server.nodes.UaDataTypeNode
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
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned
import org.eclipse.milo.opcua.stack.core.types.enumerated.StructureType
import org.eclipse.milo.opcua.stack.core.types.structured.StructureDefinition
import org.eclipse.milo.opcua.stack.core.types.structured.StructureField

class OidValueType(val oid: String, val value: Variant) : UaStructuredType {

    constructor(oid: String, value: Any?) : this(oid, Variant(value))

    constructor(oidValue: OidValue<*>) : this(oidValue.oid.numeric, oidValue.value)

    override fun getTypeId(): ExpandedNodeId {
        return TYPE_ID
    }

    override fun getBinaryEncodingId(): ExpandedNodeId {
        return BINARY_ENCODING_ID
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
        val TYPE_ID: ExpandedNodeId =
            ExpandedNodeId.parse(
                String.format(
                    "nsu=%s;s=%s",
                    SnmpNamespace.NAMESPACE_URI,
                    "DataType.OidValueStructType",
                )
            )

        val BINARY_ENCODING_ID: ExpandedNodeId =
            ExpandedNodeId.parse(
                String.format(
                    "nsu=%s;s=%s",
                    SnmpNamespace.NAMESPACE_URI,
                    "DataType.OidValueStructType.BinaryEncoding",
                )
            )

        fun register(namespace: ManagedNamespace) {
            val dataTypeId: NodeId = TYPE_ID.toNodeIdOrThrow(namespace.nodeContext.namespaceTable)
            val binaryEncodingId: NodeId =
                BINARY_ENCODING_ID.toNodeIdOrThrow(namespace.nodeContext.namespaceTable)

            val dataTypeNode =
                UaDataTypeNode(
                        namespace.nodeContext,
                        dataTypeId,
                        QualifiedName(namespace.namespaceIndex, "OidValueType"),
                        LocalizedText.english("OidValueType"),
                        LocalizedText.NULL_VALUE,
                        Unsigned.uint(0),
                        Unsigned.uint(0),
                        false,
                    )
                    .apply {
                        addReference(
                            Reference(
                                dataTypeId,
                                NodeIds.HasSubtype,
                                NodeIds.Structure.expanded(),
                                Reference.Direction.INVERSE,
                            )
                        )
                        dataTypeDefinition =
                            StructureDefinition(
                                binaryEncodingId,
                                NodeIds.Structure,
                                StructureType.Structure,
                                arrayOf(
                                    StructureField(
                                        "oid",
                                        LocalizedText.NULL_VALUE,
                                        NodeIds.String,
                                        ValueRanks.Scalar,
                                        null,
                                        namespace.nodeContext.server.config.limits.maxStringLength,
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
                    }

            namespace.nodeManager.addNode(dataTypeNode)

            namespace.nodeContext.server.staticDataTypeManager.registerType(
                dataTypeId,
                Codec(),
                binaryEncodingId,
                null,
                null,
            )
        }
    }
}
