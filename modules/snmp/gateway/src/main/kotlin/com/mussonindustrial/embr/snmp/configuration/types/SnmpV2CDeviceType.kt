package com.mussonindustrial.embr.snmp.configuration.types

import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta
import com.inductiveautomation.ignition.gateway.localdb.persistence.ReferenceField
import com.inductiveautomation.ignition.gateway.opcua.server.api.Device
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceType
import com.mussonindustrial.embr.snmp.configuration.records.SnmpV2CDeviceRecord
import com.mussonindustrial.embr.snmp.configuration.settings.SnmpV2CDeviceSettings
import com.mussonindustrial.embr.snmp.devices.SnmpContext
import com.mussonindustrial.embr.snmp.devices.SnmpDeviceImpl
import org.snmp4j.CommunityTarget
import org.snmp4j.Snmp
import org.snmp4j.mp.SnmpConstants
import org.snmp4j.smi.Address
import org.snmp4j.smi.GenericAddress
import org.snmp4j.smi.OctetString
import org.snmp4j.transport.DefaultUdpTransportMapping

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

        val snmpContext = Context(context, settings, snmpSettings)
        return SnmpDeviceImpl(snmpContext)
    }

    override fun getSettingsRecordType(): RecordMeta<out PersistentRecord> {
        return SnmpV2CDeviceRecord.META
    }

    override fun getSettingsRecordForeignKey(): ReferenceField<*> {
        return SnmpV2CDeviceRecord.DEVICE_SETTINGS
    }

    class Context(
        override val deviceContext: DeviceContext,
        override val deviceSettings: DeviceSettingsRecord,
        override val snmpSettings: SnmpV2CDeviceSettings,
    ) : SnmpContext<SnmpV2CDeviceSettings> {

        val address: Address =
            GenericAddress.parse(("udp:" + snmpSettings.hostname + "/" + snmpSettings.port))

        override val readTarget =
            CommunityTarget(address, OctetString(snmpSettings.communityRead)).apply {
                version = SnmpConstants.version2c
                timeout = snmpSettings.timeout
            }
        override val writeTarget =
            snmpSettings.communityWrite?.let {
                CommunityTarget(address, OctetString(snmpSettings.communityWrite)).apply {
                    version = SnmpConstants.version1
                    timeout = snmpSettings.timeout
                }
            }

        val transportMapping = DefaultUdpTransportMapping()
        override val snmp = Snmp(transportMapping)
    }
}
