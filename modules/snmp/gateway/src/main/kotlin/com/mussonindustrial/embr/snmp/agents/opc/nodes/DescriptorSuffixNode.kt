package com.mussonindustrial.embr.snmp.agents.opc.nodes

import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDevice
import com.mussonindustrial.embr.snmp.model.ObjectModel
import org.eclipse.milo.opcua.sdk.core.AccessLevel
import org.eclipse.milo.opcua.sdk.core.ValueRank
import org.eclipse.milo.opcua.stack.core.OpcUaDataType
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte

sealed class DescriptorSuffixNode(val name: String) {

    data class Context(val device: SnmpAgentDevice, val descriptor: ObjectModel.Descriptor)

    open fun browseName(context: Context): QualifiedName = QualifiedName(0, name)

    open fun displayName(context: Context): LocalizedText = LocalizedText.english(name)

    open fun description(context: Context): LocalizedText = LocalizedText.english("")

    open fun accessLevel(context: Context): UByte? = AccessLevel.toValue(AccessLevel.READ_ONLY)

    abstract fun dataType(context: Context): NodeId

    abstract fun valueRank(context: Context): Int

    abstract fun value(context: Context): Any?

    object DataType : DescriptorSuffixNode("DataType") {
        override fun dataType(context: Context): NodeId = OpcUaDataType.String.nodeId

        override fun valueRank(context: Context) = ValueRank.Scalar.value

        override fun value(context: Context) =
            (context.descriptor as? ObjectModel.ValueDescriptor)?.snmpDataType?.name
    }

    object ExpectedSize : DescriptorSuffixNode("ExpectedSize") {
        override fun dataType(context: Context): NodeId = OpcUaDataType.UInt32.nodeId

        override fun valueRank(context: Context) = ValueRank.Scalar.value

        override fun value(context: Context) =
            (context.descriptor as? ObjectModel.ValueDescriptor)?.expectedSize
    }

    companion object {
        val ALL = listOf(DataType, ExpectedSize)
        private val byName = ALL.associateBy { it.name }

        fun from(name: String): DescriptorSuffixNode? = byName[name]
    }
}
