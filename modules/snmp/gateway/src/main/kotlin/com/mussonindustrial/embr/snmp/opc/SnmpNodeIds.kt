package com.mussonindustrial.embr.snmp.opc

import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId

class SnmpNodeIds(nodeIdFactory: (Any) -> NodeId) {

    val Null = nodeIdFactory(0)
    val Int32 = nodeIdFactory(1)
    val UInt32 = nodeIdFactory(2)
    val OctetString = nodeIdFactory(3)
    val Oid = nodeIdFactory(4)
    val IpAddress = nodeIdFactory(5)
    val Counter32 = nodeIdFactory(6)
    val Counter64 = nodeIdFactory(7)
    val Gauge32 = nodeIdFactory(8)
    val TimeTicks = nodeIdFactory(9)

    val SnmpAgentDeviceType = nodeIdFactory(1000)
    val SnmpAgentDeviceType_ReadTable = nodeIdFactory(1010)
    val SnmpAgentDeviceType_ReadTable_InputArguments = nodeIdFactory(1011)
    val SnmpAgentDeviceType_ReadTable_OutputArguments = nodeIdFactory(1012)
    val SnmpAgentDeviceType_Walk = nodeIdFactory(1020)
    val SnmpAgentDeviceType_Walk_InputArguments = nodeIdFactory(1021)
    val SnmpAgentDeviceType_Walk_OutputArguments = nodeIdFactory(1022)

    val OidValue = nodeIdFactory(2000)
    val OidValue_Encoding_DefaultBinary = nodeIdFactory(2001)
}
