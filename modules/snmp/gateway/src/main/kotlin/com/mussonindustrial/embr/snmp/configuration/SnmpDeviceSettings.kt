package com.mussonindustrial.embr.snmp.configuration

interface SnmpDeviceSettings {
    val hostname: String
    val port: Int
    val connectionTimeout: Long
    val healthcheckOid: String
}
