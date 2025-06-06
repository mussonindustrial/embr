package com.mussonindustrial.embr.snmp.configuration

import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta
import com.inductiveautomation.ignition.gateway.localdb.persistence.ReferenceField
import com.inductiveautomation.ignition.gateway.opcua.server.api.Device
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceType
import com.mussonindustrial.embr.snmp.devices.SnmpV2CDevice
import com.mussonindustrial.embr.snmp.devices.asSnmpDeviceContext

object SnmpV2CDeviceType :
    DeviceType(
        "embr-snmp-v2c",
        "Snmp.device.SnmpV2CDevice.DisplayName",
        "Snmp.device.SnmpV2CDevice.Description",
    ) {
    @Suppress("unused") private fun readResolve(): Any = SnmpV2CDeviceType

    override fun createDevice(context: DeviceContext, settings: DeviceSettingsRecord): Device {
        val snmpSettings =
            findProfileSettingsRecord<SnmpV2CDeviceRecord>(context.getGatewayContext(), settings)

        return SnmpV2CDevice(context.asSnmpDeviceContext(), settings, snmpSettings)
    }

    override fun getSettingsRecordType(): RecordMeta<out PersistentRecord> {
        return SnmpV2CDeviceRecord.META
    }

    override fun getSettingsRecordForeignKey(): ReferenceField<*> {
        return SnmpV2CDeviceRecord.DEVICE_SETTINGS
    }
}
