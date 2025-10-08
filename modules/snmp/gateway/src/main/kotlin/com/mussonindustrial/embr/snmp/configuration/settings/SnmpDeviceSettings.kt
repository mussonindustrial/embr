package com.mussonindustrial.embr.snmp.configuration.settings

interface SnmpDeviceSettings {
    val address: String
    val timeout: Int
    val healthcheckFrequency: Int?
    val healthcheckOid: String?
}
