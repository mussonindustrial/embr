package com.mussonindustrial.embr.snmp.agents.opc.nodes

import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import com.mussonindustrial.embr.snmp.model.ObjectModel
import org.eclipse.milo.opcua.sdk.core.AccessLevel
import org.eclipse.milo.opcua.sdk.core.ValueRank
import org.eclipse.milo.opcua.stack.core.AttributeId
import org.eclipse.milo.opcua.stack.core.OpcUaDataType
import org.eclipse.milo.opcua.stack.core.StatusCodes
import org.eclipse.milo.opcua.stack.core.UaException
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass

sealed class DescriptorSuffixNode(val name: String) {

    data class Context(
        val nodeId: NodeId,
        val device: SnmpAgentDevice,
        val descriptor: ObjectModel.Descriptor,
    )

    open fun getBrowseName(context: Context): QualifiedName = QualifiedName(0, name)

    open fun getDisplayName(context: Context): LocalizedText = LocalizedText.english(name)

    open fun getDescription(context: Context): LocalizedText = LocalizedText.english("")

    open fun getAccessLevel(context: Context): UByte? = AccessLevel.toValue(AccessLevel.READ_ONLY)

    abstract fun getDataType(context: Context): NodeId

    abstract fun getValueRank(context: Context): Int

    abstract fun getValue(context: Context): Any?

    open fun readAttribute(context: Context, attributeId: AttributeId?): Any? {
        return when (attributeId) {
            AttributeId.NodeId -> context.nodeId
            AttributeId.NodeClass -> NodeClass.Variable
            AttributeId.BrowseName -> getBrowseName(context)
            AttributeId.DisplayName -> getDisplayName(context)
            AttributeId.Description -> getDescription(context)

            AttributeId.DataType -> getDataType(context)
            AttributeId.ValueRank -> getValueRank(context)
            AttributeId.ArrayDimensions -> null

            AttributeId.AccessLevel,
            AttributeId.UserAccessLevel -> getAccessLevel(context)

            AttributeId.Historizing -> false
            AttributeId.Value -> getValue(context)

            AttributeId.WriteMask,
            AttributeId.UserWriteMask -> UInteger.valueOf(0)

            else ->
                throw UaException(StatusCodes.Bad_AttributeIdInvalid, "attributeId: $attributeId")
        }
    }

    object DataType : DescriptorSuffixNode("DataType") {
        override fun getDataType(context: Context): NodeId = OpcUaDataType.String.nodeId

        override fun getValueRank(context: Context) = ValueRank.Scalar.value

        override fun getValue(context: Context) =
            (context.descriptor as? ObjectModel.ValueDescriptor)?.snmpDataType?.name
    }

    object ExpectedSize : DescriptorSuffixNode("ExpectedSize") {
        override fun getDataType(context: Context): NodeId = OpcUaDataType.UInt32.nodeId

        override fun getValueRank(context: Context) = ValueRank.Scalar.value

        override fun getValue(context: Context) =
            (context.descriptor as? ObjectModel.ValueDescriptor)?.expectedSize
    }

    companion object {
        val ALL = listOf(DataType, ExpectedSize)
        private val byName = ALL.associateBy { it.name }

        fun from(name: String): DescriptorSuffixNode? = byName[name]
    }
}
