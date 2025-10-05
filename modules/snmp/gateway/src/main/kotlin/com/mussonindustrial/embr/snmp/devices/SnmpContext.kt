package com.mussonindustrial.embr.snmp.devices

import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceProfileConfig
import com.mussonindustrial.embr.snmp.configuration.extensions.SnmpDeviceConfig
import org.snmp4j.Snmp
import org.snmp4j.Target
import org.snmp4j.smi.Address
import org.snmp4j.util.PDUFactory

interface SnmpContext<T : SnmpDeviceConfig> {
    val deviceContext: DeviceContext
    val deviceConfig: DeviceProfileConfig
    val snmpConfig: T

    val readTarget: Target<Address>
    val writeTarget: Target<Address>?
    val pduFactory: PDUFactory
    val snmp: Snmp
}
