package com.mussonindustrial.embr.snmp.typing

import com.inductiveautomation.ignition.common.TypeUtilities
import org.eclipse.milo.opcua.stack.core.OpcUaDataType
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.snmp4j.smi.Address
import org.snmp4j.smi.Counter32
import org.snmp4j.smi.Counter64
import org.snmp4j.smi.Gauge32
import org.snmp4j.smi.Integer32
import org.snmp4j.smi.IpAddress
import org.snmp4j.smi.OID
import org.snmp4j.smi.OctetString
import org.snmp4j.smi.UnsignedInteger32
import org.snmp4j.smi.Variable

enum class SnmpDataType(val uaDataType: NodeId, val variableOfType: (Any?) -> Variable) {
    Int32(OpcUaDataType.Int32.nodeId, { Integer32(TypeUtilities.toInteger(it)) }),
    UInt32(OpcUaDataType.UInt32.nodeId, { UnsignedInteger32(TypeUtilities.toLong(it)) }),
    Gauge32(OpcUaDataType.UInt32.nodeId, { Gauge32(TypeUtilities.toLong(it)) }),
    Counter32(OpcUaDataType.UInt64.nodeId, { Counter32(TypeUtilities.toLong(it)) }),
    Counter64(OpcUaDataType.UInt64.nodeId, { Counter64(TypeUtilities.toLong(it)) }),
    OctetString(OpcUaDataType.String.nodeId, { OctetString(TypeUtilities.toString(it)) }),
    Oid(OpcUaDataType.String.nodeId, { OID(TypeUtilities.toString(it)) }),
    IpAddress(OpcUaDataType.String.nodeId, { IpAddress(TypeUtilities.toString(it)) }),
}

val Variable.snmpDataType: SnmpDataType
    get() =
        when (this) {
            is OID -> SnmpDataType.Oid
            is Gauge32 -> SnmpDataType.Gauge32
            is Counter32 -> SnmpDataType.Counter32
            is Counter64 -> SnmpDataType.Counter64
            is Integer32 -> SnmpDataType.Int32
            is UnsignedInteger32 -> SnmpDataType.UInt32
            is Address -> SnmpDataType.IpAddress
            is OctetString -> SnmpDataType.OctetString
            else -> SnmpDataType.OctetString
        }
