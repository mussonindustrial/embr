package com.mussonindustrial.embr.snmp.devices

import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.mussonindustrial.embr.snmp.configuration.settings.SnmpV3DeviceSettings
import org.snmp4j.Snmp
import org.snmp4j.Target
import org.snmp4j.fluent.SnmpBuilder
import org.snmp4j.mp.SnmpConstants
import org.snmp4j.smi.Address
import org.snmp4j.smi.GenericAddress
import org.snmp4j.smi.OctetString

class SnmpV3Context(
    override val deviceContext: DeviceContext,
    override val deviceSettings: DeviceSettingsRecord,
    override val snmpSettings: SnmpV3DeviceSettings,
) : SnmpContext<SnmpV3DeviceSettings> {

    val address: Address =
        GenericAddress.parse(("udp:" + snmpSettings.hostname + "/" + snmpSettings.port))
    val securityName = OctetString(snmpSettings.username)
    val authoritativeEngineId = byteArrayOf()

    val builder = SnmpBuilder()

    override val target: Target<Address> =
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

    override val snmp: Snmp = builder.udp().v3().usm().build()
}
