package com.mussonindustrial.embr.snmp.devices

import com.inductiveautomation.ignition.common.util.LoggerEx
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.snmp.configuration.settings.SnmpDeviceSettings
import org.snmp4j.Snmp
import org.snmp4j.Target
import org.snmp4j.smi.Address
import org.snmp4j.util.PDUFactory

interface SnmpContext<T : SnmpDeviceSettings> {
    val deviceContext: DeviceContext
    val deviceSettings: DeviceSettingsRecord
    val snmpSettings: T

    val readTarget: Target<Address>
    val writeTarget: Target<Address>?
    val pduFactory: PDUFactory
    val snmp: Snmp

    val logger: LoggerEx
        get() =
            this.getLoggerEx(
                mapOf(
                    "device-name" to this.deviceContext.getName(),
                    "device-type" to this.deviceSettings.type,
                    "address" to this.snmpSettings.address,
                )
            )
}
