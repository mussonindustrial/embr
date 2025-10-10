package com.mussonindustrial.embr.snmp.agents.configuration.settings

interface SnmpAgentDeviceSettings {
    val address: String
    val timeout: Int
    val healthcheckFrequency: Int?
    val healthcheckOid: String?
}
