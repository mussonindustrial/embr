package com.mussonindustrial.embr.snmp.devices

import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.mussonindustrial.embr.snmp.configuration.SnmpV3DeviceSettings
import com.mussonindustrial.embr.snmp.utils.addLifecycle
import org.snmp4j.CommunityTarget
import org.snmp4j.Snmp
import org.snmp4j.mp.SnmpConstants
import org.snmp4j.smi.Address
import org.snmp4j.smi.GenericAddress
import org.snmp4j.smi.OctetString
import org.snmp4j.transport.DefaultUdpTransportMapping

class SnmpV3Device(
    deviceContext: SnmpDeviceContext,
    deviceSettings: DeviceSettingsRecord,
    snmpSettings: SnmpV3DeviceSettings,
) : AbstractSnmpDevice<SnmpV3DeviceSettings>(deviceContext, deviceSettings, snmpSettings) {

    val address: Address =
        GenericAddress.parse(("udp:" + snmpSettings.hostname + "/" + snmpSettings.port))
    val community = OctetString("public")
    override val target =
        CommunityTarget(address, community).apply { version = SnmpConstants.version3 }

    val transportMapping = DefaultUdpTransportMapping()
    override val snmp = Snmp(transportMapping)

    init {
        lifecycleManager.addLifecycle(snmp)
    }
}
