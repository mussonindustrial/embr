package com.mussonindustrial.embr.snmp.configuration.settings

interface SnmpDeviceSettings {
    val hostname: String
    val port: Int
    val connectionTimeout: Long
    val healthcheckOid: String
}
