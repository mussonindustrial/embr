package com.mussonindustrial.embr.snmp.opc.types

import com.inductiveautomation.ignition.common.TypeUtilities
import com.mussonindustrial.embr.snmp.opc.SnmpNamespace
import com.mussonindustrial.embr.snmp.opc.addNode
import com.mussonindustrial.embr.snmp.opc.addSubtypeOf
import org.eclipse.milo.opcua.sdk.server.nodes.UaDataTypeNode
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext
import org.eclipse.milo.opcua.stack.core.NodeIds
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint
import org.eclipse.milo.opcua.stack.core.types.structured.AccessRestrictionType
import org.snmp4j.smi.Variable

enum class SnmpDataType(
    val nodeId: NodeId,
    val parentType: NodeId,
    val backingVariableClass: Class<out Variable>,
    private val factory: (Any?) -> Variable,
) {
    Null(
        SnmpNamespace.NodesIds.Null,
        NodeIds.BaseDataType,
        org.snmp4j.smi.Null::class.java,
        { org.snmp4j.smi.Null() },
    ),
    Int32(
        SnmpNamespace.NodesIds.Int32,
        NodeIds.Int32,
        org.snmp4j.smi.Integer32::class.java,
        { org.snmp4j.smi.Integer32(TypeUtilities.toInteger(it)) },
    ),
    UInt32(
        SnmpNamespace.NodesIds.UInt32,
        NodeIds.UInt32,
        org.snmp4j.smi.UnsignedInteger32::class.java,
        { org.snmp4j.smi.UnsignedInteger32(TypeUtilities.toInteger(it)) },
    ),
    OctetString(
        SnmpNamespace.NodesIds.OctetString,
        NodeIds.String,
        org.snmp4j.smi.OctetString::class.java,
        { org.snmp4j.smi.OctetString(TypeUtilities.toString(it)) },
    ),
    Oid(
        SnmpNamespace.NodesIds.Oid,
        NodeIds.String,
        org.snmp4j.smi.OID::class.java,
        { org.snmp4j.smi.OID(TypeUtilities.toString(it)) },
    ),
    IpAddress(
        SnmpNamespace.NodesIds.IpAddress,
        NodeIds.String,
        org.snmp4j.smi.IpAddress::class.java,
        { org.snmp4j.smi.IpAddress(TypeUtilities.toString(it)) },
    ),
    Counter32(
        SnmpNamespace.NodesIds.Counter32,
        NodeIds.UInt32,
        org.snmp4j.smi.Counter32::class.java,
        { org.snmp4j.smi.Counter32(TypeUtilities.toLong(it)) },
    ),
    Counter64(
        SnmpNamespace.NodesIds.Counter64,
        NodeIds.UInt64,
        org.snmp4j.smi.Counter64::class.java,
        { org.snmp4j.smi.Counter64(TypeUtilities.toLong(it)) },
    ),
    Gauge32(
        SnmpNamespace.NodesIds.Gauge32,
        NodeIds.UInt64,
        org.snmp4j.smi.Gauge32::class.java,
        { org.snmp4j.smi.Gauge32(TypeUtilities.toLong(it)) },
    ),
    TimeTicks(
        SnmpNamespace.NodesIds.TimeTicks,
        NodeIds.UInt64,
        org.snmp4j.smi.TimeTicks::class.java,
        { org.snmp4j.smi.TimeTicks(TypeUtilities.toLong(it)) },
    );

    companion object {
        private val byClass: Map<Class<out Variable>, SnmpDataType> =
            entries.associateBy { it.backingVariableClass }

        fun registerAll(nodeContext: UaNodeContext) {
            entries.forEach { it.register(nodeContext) }
        }

        fun of(variable: Variable): SnmpDataType {
            byClass[variable.javaClass]?.let {
                return it
            }
            return entries.firstOrNull {
                it.backingVariableClass.isAssignableFrom(variable.javaClass)
            }
                ?: throw IllegalArgumentException(
                    "No SNMP type for variable [${variable.javaClass}] $variable"
                )
        }

        fun variableOfType(dataType: SnmpDataType, value: Any?): Variable = dataType.factory(value)
    }

    private fun register(nodeContext: UaNodeContext) {
        UaDataTypeNode(
                nodeContext,
                nodeId,
                SnmpNamespace.qualifiedName(name),
                LocalizedText.english(name),
                LocalizedText.english(""),
                uint(0),
                uint(0),
                false,
            )
            .apply {
                addNode(nodeContext.nodeManager)
                addSubtypeOf(parentType.expanded())

                accessRestrictions = AccessRestrictionType.of()
            }
    }
}
