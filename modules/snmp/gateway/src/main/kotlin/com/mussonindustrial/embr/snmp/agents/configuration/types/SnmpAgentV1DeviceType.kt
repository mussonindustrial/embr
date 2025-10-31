package com.mussonindustrial.embr.snmp.agents.configuration.types

import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta
import com.inductiveautomation.ignition.gateway.localdb.persistence.ReferenceField
import com.inductiveautomation.ignition.gateway.opcua.server.api.Device
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceType
import com.mussonindustrial.embr.snmp.agents.configuration.records.SnmpAgentV1DeviceRecord
import com.mussonindustrial.embr.snmp.agents.context.SnmpAgentV1Context
import com.mussonindustrial.embr.snmp.agents.devices.SnmpAgentDeviceImpl

object SnmpAgentV1DeviceType :
    DeviceType(
        "embr-snmp-agent-v1",
        "Snmp.device.SnmpAgentV1.DisplayName",
        "Snmp.device.SnmpAgentV1.Description",
    ) {
    @Suppress("unused") private fun readResolve(): Any = SnmpAgentV1DeviceType

    override fun createDevice(context: DeviceContext, settings: DeviceSettingsRecord): Device {
        val snmpSettings =
            findProfileSettingsRecord<SnmpAgentV1DeviceRecord>(
                context.getGatewayContext(),
                settings,
            )
        val snmpContext = SnmpAgentV1Context(context, settings, snmpSettings)
        return SnmpAgentDeviceImpl(snmpContext)
    }

    override fun getSettingsRecordType(): RecordMeta<out PersistentRecord> {
        return SnmpAgentV1DeviceRecord.META
    }

    override fun getSettingsRecordForeignKey(): ReferenceField<*> {
        return SnmpAgentV1DeviceRecord.DEVICE_SETTINGS
    }
}
