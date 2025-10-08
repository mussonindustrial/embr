package com.mussonindustrial.embr.snmp.configuration.settings

interface SnmpV1DeviceSettings : SnmpDeviceSettings {
    val communityRead: String
    val communityWrite: String?
}
