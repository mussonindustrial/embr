package com.mussonindustrial.embr.snmp.agents.configuration.settings

interface SnmpAgentV1DeviceSettings : SnmpAgentDeviceSettings {
    val communityRead: String
    val communityWrite: String?
}
