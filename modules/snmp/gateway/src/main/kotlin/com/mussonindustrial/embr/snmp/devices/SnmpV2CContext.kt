package com.mussonindustrial.embr.snmp.devices

import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.mussonindustrial.embr.snmp.configuration.settings.SnmpV2CDeviceSettings
import org.snmp4j.CommunityTarget
import org.snmp4j.Snmp
import org.snmp4j.mp.SnmpConstants
import org.snmp4j.smi.Address
import org.snmp4j.smi.GenericAddress
import org.snmp4j.smi.OctetString
import org.snmp4j.transport.DefaultUdpTransportMapping

class SnmpV2CContext(
    override val deviceContext: DeviceContext,
    override val deviceSettings: DeviceSettingsRecord,
    override val snmpSettings: SnmpV2CDeviceSettings,
) : SnmpContext<SnmpV2CDeviceSettings> {

    val address: Address =
        GenericAddress.parse(("udp:" + snmpSettings.hostname + "/" + snmpSettings.port))
    val community = OctetString(snmpSettings.community)
    override val target =
        CommunityTarget(address, community).apply { version = SnmpConstants.version2c }

    val transportMapping = DefaultUdpTransportMapping()
    override val snmp = Snmp(transportMapping)
}
