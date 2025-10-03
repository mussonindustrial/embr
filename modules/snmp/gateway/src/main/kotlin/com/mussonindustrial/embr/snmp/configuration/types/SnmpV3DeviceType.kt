package com.mussonindustrial.embr.snmp.configuration.types

import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta
import com.inductiveautomation.ignition.gateway.localdb.persistence.ReferenceField
import com.inductiveautomation.ignition.gateway.opcua.server.api.Device
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceType
import com.mussonindustrial.embr.snmp.configuration.protocols.AuthenticationProtocol
import com.mussonindustrial.embr.snmp.configuration.protocols.PrivacyProtocol
import com.mussonindustrial.embr.snmp.configuration.records.SnmpV3DeviceRecord
import com.mussonindustrial.embr.snmp.configuration.settings.SnmpV3DeviceSettings
import com.mussonindustrial.embr.snmp.devices.SnmpContext
import com.mussonindustrial.embr.snmp.devices.SnmpDeviceImpl
import org.snmp4j.DirectUserTarget
import org.snmp4j.Snmp
import org.snmp4j.smi.Address
import org.snmp4j.smi.GenericAddress
import org.snmp4j.smi.OctetString
import org.snmp4j.transport.DefaultUdpTransportMapping

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

        val snmpContext = Context(context, settings, snmpSettings)
        return SnmpDeviceImpl(snmpContext)
    }

    override fun getSettingsRecordType(): RecordMeta<out PersistentRecord> {
        return SnmpV3DeviceRecord.META
    }

    override fun getSettingsRecordForeignKey(): ReferenceField<*> {
        return SnmpV3DeviceRecord.DEVICE_SETTINGS
    }

    class Context(
        override val deviceContext: DeviceContext,
        override val deviceSettings: DeviceSettingsRecord,
        override val snmpSettings: SnmpV3DeviceSettings,
    ) : SnmpContext<SnmpV3DeviceSettings> {

        val address: Address =
            GenericAddress.parse(("udp:" + snmpSettings.hostname + "/" + snmpSettings.port))

        val authenticationPassphrase =
            snmpSettings.authPassword
                ?.takeIf { snmpSettings.authProtocol != AuthenticationProtocol.NONE }
                ?.let { OctetString(it) }

        val privacyPassphrase =
            snmpSettings.privacyPassword
                ?.takeIf { snmpSettings.privacyProtocol != PrivacyProtocol.NONE }
                ?.let { OctetString(it) }

        val target =
            DirectUserTarget(
                    address,
                    OctetString(snmpSettings.username),
                    snmpSettings.authProtocol.mappedProtocol,
                    authenticationPassphrase,
                    snmpSettings.privacyProtocol.mappedProtocol,
                    privacyPassphrase,
                )
                .apply { timeout = snmpSettings.timeout.toLong() }

        override val readTarget = target
        override val writeTarget = target

        val transportMapping = DefaultUdpTransportMapping()
        override val snmp = Snmp(transportMapping)
    }
}
