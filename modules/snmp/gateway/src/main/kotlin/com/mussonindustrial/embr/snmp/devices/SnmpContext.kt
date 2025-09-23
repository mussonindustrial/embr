package com.mussonindustrial.embr.snmp.devices

import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.mussonindustrial.embr.snmp.configuration.settings.SnmpDeviceSettings
import org.snmp4j.Snmp
import org.snmp4j.Target
import org.snmp4j.smi.Address

interface SnmpContext<T : SnmpDeviceSettings> {
    val deviceContext: DeviceContext
    val deviceSettings: DeviceSettingsRecord
    val snmpSettings: T

    val readTarget: Target<Address>
    val writeTarget: Target<Address>?
    val snmp: Snmp
}
