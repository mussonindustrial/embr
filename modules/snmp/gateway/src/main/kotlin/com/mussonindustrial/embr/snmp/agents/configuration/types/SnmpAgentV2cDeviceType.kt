package com.mussonindustrial.embr.snmp.agents.configuration.types

import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta
import com.inductiveautomation.ignition.gateway.localdb.persistence.ReferenceField
import com.inductiveautomation.ignition.gateway.opcua.server.api.Device
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceType
import com.mussonindustrial.embr.snmp.agents.configuration.records.SnmpAgentV2cDeviceRecord
import com.mussonindustrial.embr.snmp.agents.context.SnmpAgentV2cContext
import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDeviceImpl

object SnmpAgentV2cDeviceType :
    DeviceType(
        "embr-snmp-agent-v2c",
        "Snmp.device.SnmpAgentV2c.DisplayName",
        "Snmp.device.SnmpAgentV2c.Description",
    ) {
    @Suppress("unused") private fun readResolve(): Any = SnmpAgentV2cDeviceType

    override fun createDevice(context: DeviceContext, settings: DeviceSettingsRecord): Device {
        val snmpSettings =
            findProfileSettingsRecord<SnmpAgentV2cDeviceRecord>(
                context.getGatewayContext(),
                settings,
            )
        val snmpContext = SnmpAgentV2cContext(context, settings, snmpSettings)
        return SnmpAgentDeviceImpl(snmpContext)
    }

    override fun getSettingsRecordType(): RecordMeta<out PersistentRecord> {
        return SnmpAgentV2cDeviceRecord.META
    }

    override fun getSettingsRecordForeignKey(): ReferenceField<*> {
        return SnmpAgentV2cDeviceRecord.DEVICE_SETTINGS
    }
}
