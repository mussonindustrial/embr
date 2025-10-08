package com.mussonindustrial.embr.snmp.configuration.settings

import com.mussonindustrial.embr.snmp.configuration.protocols.AuthenticationProtocol
import com.mussonindustrial.embr.snmp.configuration.protocols.PrivacyProtocol

interface SnmpV3DeviceSettings : SnmpDeviceSettings {
    val username: String
    val authProtocol: AuthenticationProtocol
    val authPassword: String?
    val privacyProtocol: PrivacyProtocol
    val privacyPassword: String?
}
