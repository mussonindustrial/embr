package com.mussonindustrial.embr.snmp.agents.context

import com.inductiveautomation.ignition.common.util.LoggerEx
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceSettingsRecord
import com.mussonindustrial.embr.snmp.configuration.settings.SnmpDeviceSettings
import org.eclipse.milo.opcua.sdk.server.Lifecycle
import org.snmp4j.Snmp
import org.snmp4j.Target
import org.snmp4j.smi.Address
import org.snmp4j.util.PDUFactory

interface SnmpAgentContext<T : SnmpDeviceSettings> : Lifecycle {
    val deviceContext: DeviceContext
    val deviceSettings: DeviceSettingsRecord
    val snmpSettings: T

    val readTarget: Target<Address>
    val writeTarget: Target<Address>?
    val pduFactory: PDUFactory
    val snmp: Snmp

    val logger: LoggerEx
}
