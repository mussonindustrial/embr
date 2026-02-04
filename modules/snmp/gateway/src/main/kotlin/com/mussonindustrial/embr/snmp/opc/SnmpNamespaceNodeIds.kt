package com.mussonindustrial.embr.snmp.opc

class SnmpNamespaceNodeIds() {

    val Null = SnmpNamespace.nodeId(0)
    val Int32 = SnmpNamespace.nodeId(1)
    val UInt32 = SnmpNamespace.nodeId(2)
    val OctetString = SnmpNamespace.nodeId(3)
    val Oid = SnmpNamespace.nodeId(4)
    val IpAddress = SnmpNamespace.nodeId(5)
    val Counter32 = SnmpNamespace.nodeId(6)
    val Counter64 = SnmpNamespace.nodeId(7)
    val Gauge32 = SnmpNamespace.nodeId(8)
    val TimeTicks = SnmpNamespace.nodeId(9)

    val SnmpAgentDeviceType = SnmpNamespace.nodeId(1000)
    val SnmpAgentDeviceType_ReadTable = SnmpNamespace.nodeId(1010)
    val SnmpAgentDeviceType_ReadTable_InputArguments = SnmpNamespace.nodeId(1011)
    val SnmpAgentDeviceType_ReadTable_OutputArguments = SnmpNamespace.nodeId(1012)
    val SnmpAgentDeviceType_Walk = SnmpNamespace.nodeId(1020)
    val SnmpAgentDeviceType_Walk_InputArguments = SnmpNamespace.nodeId(1021)
    val SnmpAgentDeviceType_Walk_OutputArguments = SnmpNamespace.nodeId(1022)

    val OidValue = SnmpNamespace.nodeId(2000)
    val OidValue_Encoding_DefaultBinary = SnmpNamespace.nodeId(2001)
}
