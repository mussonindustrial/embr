package com.mussonindustrial.embr.snmp

import com.codahale.metrics.health.HealthCheckRegistry
import com.inductiveautomation.ignition.common.execution.ExecutionManager
import com.inductiveautomation.ignition.gateway.model.DiagnosticsManager
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.ignition.gateway.model.TelemetryManager
import com.mussonindustrial.embr.gateway.EmbrGatewayContext
import com.mussonindustrial.embr.gateway.EmbrGatewayContextImpl
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import org.snmp4j.security.SecurityModels
import org.snmp4j.security.SecurityProtocols
import org.snmp4j.security.USM
import org.snmp4j.smi.OctetString

class SnmpGatewayContext(private val context: GatewayContext) :
    EmbrGatewayContext by EmbrGatewayContextImpl(context) {
    companion object {
        lateinit var instance: SnmpGatewayContext
        const val PRIVATE_ENTERPRISE_NUMBER = 63707
    }

    val deviceTypes = listOf(SnmpV1DeviceType, SnmpV2CDeviceType, SnmpV3DeviceType)
    private val records =
        listOf(SnmpV1DeviceRecord.META, SnmpV2CDeviceRecord.META, SnmpV3DeviceRecord.META)

    val securityProtocols: SecurityProtocols =
        SecurityProtocols.getInstance().apply {
            addPredefinedProtocolSet(SecurityProtocols.SecurityProtocolSet.any)
        }
    val engineId: OctetString = OctetString.fromHexStringPairs("63707")
    val usm =
        USM(securityProtocols, engineId, 0).apply {
            SecurityModels.getInstance().addSecurityModel(this)
        }

    val snmpExecutionManager: ExecutionManager =
        context.createExecutionManager(
            "Embr SNMP Driver",
            3,
            object : ThreadFactory {
                private val counter = AtomicInteger(0)

                override fun newThread(r: Runnable): Thread =
                    Thread(null, r, "embr-snmp-executor-${counter.incrementAndGet()}")
            },
        )

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
