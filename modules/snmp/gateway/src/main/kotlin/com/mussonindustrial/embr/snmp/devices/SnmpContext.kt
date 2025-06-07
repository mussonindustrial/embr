package com.mussonindustrial.embr.snmp.devices

import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.mussonindustrial.embr.snmp.configuration.settings.SnmpDeviceSettings
import org.snmp4j.AbstractTarget
import org.snmp4j.Snmp

interface SnmpContext<T : SnmpDeviceSettings> {
    val deviceContext: DeviceContext
    val deviceSettings: DeviceSettingsRecord
    val snmpSettings: T

    val target: AbstractTarget<*>
    val snmp: Snmp
}
