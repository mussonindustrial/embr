package com.mussonindustrial.embr.snmp.configuration.settings

interface SnmpDeviceSettings {
    val hostname: String
    val port: Int
    val timeout: Int
    val healthcheckFrequency: Int?
    val healthcheckOid: String?
}
