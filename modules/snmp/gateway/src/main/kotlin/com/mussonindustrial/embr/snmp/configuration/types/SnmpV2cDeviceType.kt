package com.mussonindustrial.embr.snmp.configuration.types

import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta
import com.inductiveautomation.ignition.gateway.localdb.persistence.ReferenceField
import com.inductiveautomation.ignition.gateway.opcua.server.api.Device
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceType
import com.mussonindustrial.embr.snmp.configuration.records.SnmpV2cDeviceRecord
import com.mussonindustrial.embr.snmp.context.SnmpV2cContext
import com.mussonindustrial.embr.snmp.devices.SnmpDeviceImpl

object SnmpV2cDeviceType :
    DeviceType(
        "embr-snmp-v2c",
        "Snmp.device.SnmpV2cDevice.DisplayName",
        "Snmp.device.SnmpV2cDevice.Description",
    ) {
    @Suppress("unused") private fun readResolve(): Any = SnmpV2cDeviceType

    override fun createDevice(context: DeviceContext, settings: DeviceSettingsRecord): Device {
        val snmpSettings =
            findProfileSettingsRecord<SnmpV2cDeviceRecord>(context.getGatewayContext(), settings)
        val snmpContext = SnmpV2cContext(context, settings, snmpSettings)
        return SnmpDeviceImpl(snmpContext)
    }

    override fun getSettingsRecordType(): RecordMeta<out PersistentRecord> {
        return SnmpV2cDeviceRecord.META
    }

    override fun getSettingsRecordForeignKey(): ReferenceField<*> {
        return SnmpV2cDeviceRecord.DEVICE_SETTINGS
    }
}
