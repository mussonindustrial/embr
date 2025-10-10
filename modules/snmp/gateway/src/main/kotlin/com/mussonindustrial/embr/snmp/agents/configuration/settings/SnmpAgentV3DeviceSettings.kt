package com.mussonindustrial.embr.snmp.agents.configuration.settings

import com.mussonindustrial.embr.snmp.protocols.AuthenticationProtocol
import com.mussonindustrial.embr.snmp.protocols.PrivacyProtocol

interface SnmpAgentV3DeviceSettings : SnmpAgentDeviceSettings {
    val username: String
    val authProtocol: AuthenticationProtocol
    val authPassword: String?
    val privacyProtocol: PrivacyProtocol
    val privacyPassword: String?
}
