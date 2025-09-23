package com.mussonindustrial.embr.snmp

import com.codahale.metrics.health.HealthCheckRegistry
import com.inductiveautomation.ignition.gateway.model.DiagnosticsManager
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.ignition.gateway.model.TelemetryManager
import com.mussonindustrial.embr.gateway.EmbrGatewayContext
import com.mussonindustrial.embr.gateway.EmbrGatewayContextImpl
import org.snmp4j.SNMP4JSettings
import org.snmp4j.mp.MPv3
import org.snmp4j.security.*
import org.snmp4j.smi.OctetString

class SnmpGatewayContext(private val context: GatewayContext) :
    EmbrGatewayContext by EmbrGatewayContextImpl(context) {
    companion object {
        lateinit var instance: SnmpGatewayContext
        const val PRIVATE_ENTERPRISE_NUMBER = 63707
    }

    val usm: USM
    val securityProtocols: SecurityProtocols = SecurityProtocols.getInstance()
    val securityModels: SecurityModels = SecurityModels.getInstance()

    init {
        instance = this

        SNMP4JSettings.setEnterpriseID(PRIVATE_ENTERPRISE_NUMBER)

        securityProtocols.addPredefinedProtocolSet(SecurityProtocols.SecurityProtocolSet.any)
        usm =
            USM(securityProtocols, OctetString(MPv3.createLocalEngineID()), 0).apply {
                isEngineDiscoveryEnabled = true
            }
        securityModels.addSecurityModel(usm)
    }

    override fun getHealthCheckRegistry(): HealthCheckRegistry? {
        return super.getHealthCheckRegistry()
    }

    override fun getTelemetryManager(): TelemetryManager? {
        return super.getTelemetryManager()
    }

    override fun getDiagnosticsManager(): DiagnosticsManager? {
        return super.getDiagnosticsManager()
    }
}
