package com.mussonindustrial.embr.snmp.configuration.types

import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta
import com.inductiveautomation.ignition.gateway.localdb.persistence.ReferenceField
import com.inductiveautomation.ignition.gateway.opcua.server.api.Device
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceType
import com.mussonindustrial.embr.snmp.configuration.records.SnmpV3DeviceRecord
import com.mussonindustrial.embr.snmp.devices.SnmpDeviceImpl
import com.mussonindustrial.embr.snmp.devices.SnmpV3Context

object SnmpV3DeviceType :
    DeviceType(
        "embr-snmp-v3",
        "Snmp.device.SnmpV3Device.DisplayName",
        "Snmp.device.SnmpV3Device.Description",
    ) {
    @Suppress("unused") private fun readResolve(): Any = SnmpV3DeviceType

    override fun createDevice(context: DeviceContext, settings: DeviceSettingsRecord): Device {
        val snmpSettings =
            findProfileSettingsRecord<SnmpV3DeviceRecord>(context.getGatewayContext(), settings)

        val snmpContext = SnmpV3Context(context, settings, snmpSettings)
        return SnmpDeviceImpl(snmpContext)
    }

    override fun getSettingsRecordType(): RecordMeta<out PersistentRecord> {
        return SnmpV3DeviceRecord.META
    }

    override fun getSettingsRecordForeignKey(): ReferenceField<*> {
        return SnmpV3DeviceRecord.DEVICE_SETTINGS
    }
}
