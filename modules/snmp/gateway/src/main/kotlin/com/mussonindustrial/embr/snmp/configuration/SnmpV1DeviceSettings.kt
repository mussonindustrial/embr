package com.mussonindustrial.embr.snmp.configuration

interface SnmpV1DeviceSettings : SnmpDeviceSettings {
    val community: String
}
