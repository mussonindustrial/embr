package com.mussonindustrial.embr.snmp.agents.configuration.types

import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta
import com.inductiveautomation.ignition.gateway.localdb.persistence.ReferenceField
import com.inductiveautomation.ignition.gateway.opcua.server.api.Device
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceType
import com.mussonindustrial.embr.snmp.agents.configuration.records.SnmpAgentV3DeviceRecord
import com.mussonindustrial.embr.snmp.agents.context.SnmpAgentV3Context
import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDeviceImpl

object SnmpAgentV3DeviceType :
    DeviceType(
        "embr-snmp-agent-v3",
        "Snmp.device.SnmpAgentV3.DisplayName",
        "Snmp.device.SnmpAgentV3.Description",
    ) {
    @Suppress("unused") private fun readResolve(): Any = SnmpAgentV3DeviceType

    override fun createDevice(context: DeviceContext, settings: DeviceSettingsRecord): Device {
        val snmpSettings =
            findProfileSettingsRecord<SnmpAgentV3DeviceRecord>(
                context.getGatewayContext(),
                settings,
            )
        val snmpContext = SnmpAgentV3Context(context, settings, snmpSettings)
        return SnmpAgentDeviceImpl(snmpContext)
    }

    override fun getSettingsRecordType(): RecordMeta<out PersistentRecord> {
        return SnmpAgentV3DeviceRecord.META
    }

    override fun getSettingsRecordForeignKey(): ReferenceField<*> {
        return SnmpAgentV3DeviceRecord.DEVICE_SETTINGS
    }
}
