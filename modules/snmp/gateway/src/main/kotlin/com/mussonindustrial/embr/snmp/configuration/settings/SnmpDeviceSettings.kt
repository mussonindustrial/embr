package com.mussonindustrial.embr.snmp.configuration.settings

interface SnmpDeviceSettings {
    val hostname: String
    val port: Int
    val timeout: Long
    val healthcheckFrequency: Int?
    val healthcheckOid: String?
}
