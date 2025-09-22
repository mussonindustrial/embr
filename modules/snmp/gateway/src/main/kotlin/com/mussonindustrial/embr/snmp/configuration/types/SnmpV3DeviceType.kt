package com.mussonindustrial.embr.snmp.configuration.types

import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta
import com.inductiveautomation.ignition.gateway.localdb.persistence.ReferenceField
import com.inductiveautomation.ignition.gateway.opcua.server.api.Device
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceType
import com.mussonindustrial.embr.snmp.configuration.records.SnmpV3DeviceRecord
import com.mussonindustrial.embr.snmp.configuration.settings.SnmpV3DeviceSettings
import com.mussonindustrial.embr.snmp.devices.SnmpContext
import com.mussonindustrial.embr.snmp.devices.SnmpDeviceImpl
import org.snmp4j.Snmp
import org.snmp4j.Target
import org.snmp4j.fluent.SnmpBuilder
import org.snmp4j.mp.SnmpConstants
import org.snmp4j.smi.Address
import org.snmp4j.smi.GenericAddress
import org.snmp4j.smi.OctetString

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
        val securityName = OctetString(snmpSettings.username)
        val authoritativeEngineId = byteArrayOf()

        val builder = SnmpBuilder()

        val target: Target<Address> =
            builder
                .target(address)
                .user(snmpSettings.username)
                .auth(snmpSettings.authProtocol)
                .authPassphrase(snmpSettings.authPassword)
                .priv(snmpSettings.privacyProtocol)
                .privPassphrase(snmpSettings.privacyPassword)
                .done()
                .build()
                .apply { version = SnmpConstants.version3 }

        override val readTarget = target
        override val writeTarget = target

        override val snmp: Snmp = builder.udp().v3().usm().build()
    }
}
