package com.mussonindustrial.embr.snmp.configuration

import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta
import com.inductiveautomation.ignition.gateway.localdb.persistence.ReferenceField
import com.inductiveautomation.ignition.gateway.opcua.server.api.Device
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceType
import com.mussonindustrial.embr.snmp.devices.SnmpV1Device
import com.mussonindustrial.embr.snmp.devices.asSnmpDeviceContext

object SnmpV1DeviceType :
    DeviceType(
        "embr-snmp-v1",
        "Snmp.device.SnmpV1Device.DisplayName",
        "Snmp.device.SnmpV1Device.Description",
    ) {
    @Suppress("unused") private fun readResolve(): Any = SnmpV1DeviceType

    override fun createDevice(context: DeviceContext, settings: DeviceSettingsRecord): Device {
        val snmpSettings =
            findProfileSettingsRecord<SnmpV1DeviceRecord>(context.getGatewayContext(), settings)

        return SnmpV1Device(context.asSnmpDeviceContext(), settings, snmpSettings)
    }

    override fun getSettingsRecordType(): RecordMeta<out PersistentRecord> {
        return SnmpV1DeviceRecord.META
    }

    override fun getSettingsRecordForeignKey(): ReferenceField<*> {
        return SnmpV1DeviceRecord.DEVICE_SETTINGS
    }
}
