package com.mussonindustrial.embr.snmp.configuration

import com.inductiveautomation.ignition.gateway.localdb.persistence.*
import com.inductiveautomation.ignition.gateway.opcua.server.api.Device
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceType
import com.mussonindustrial.embr.snmp.drivers.SnmpDevice

object SnmpDeviceType :
    DeviceType(
        "embr-snmp",
        "Snmp.device.SnmpDevice.DisplayName",
        "Snmp.device.SnmpDevice.Description",
    ) {
    private fun readResolve(): Any = SnmpDeviceType

    override fun createDevice(context: DeviceContext, settings: DeviceSettingsRecord): Device {
        val snmpSettings =
            findProfileSettingsRecord<SnmpDeviceSettings>(context.getGatewayContext(), settings)

        return SnmpDevice(context, settings, snmpSettings)
    }

    override fun getSettingsRecordType(): RecordMeta<out PersistentRecord> {
        return SnmpDeviceSettings.META
    }

    override fun getSettingsRecordForeignKey(): ReferenceField<*> {
        return SnmpDeviceSettings.DEVICE_SETTINGS
    }
}
