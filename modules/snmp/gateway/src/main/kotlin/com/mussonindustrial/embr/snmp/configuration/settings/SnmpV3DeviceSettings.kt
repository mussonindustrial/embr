package com.mussonindustrial.embr.snmp.configuration.settings

import org.snmp4j.fluent.TargetBuilder

interface SnmpV3DeviceSettings : SnmpDeviceSettings {
    val username: String
    val authProtocol: TargetBuilder.AuthProtocol
    val authPassword: String
    val privacyProtocol: TargetBuilder.PrivProtocol
    val privacyPassword: String
}
