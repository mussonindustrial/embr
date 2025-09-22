package com.mussonindustrial.embr.snmp.configuration.settings

interface SnmpDeviceSettings {
    val hostname: String
    val port: Int
    val healthcheckFrequency: Long
    val healthcheckOid: String
}
