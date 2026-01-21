package com.mussonindustrial.embr.snmp

import com.codahale.metrics.health.HealthCheckRegistry
import com.inductiveautomation.ignition.common.execution.ExecutionManager
import com.inductiveautomation.ignition.gateway.model.DiagnosticsManager
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.ignition.gateway.model.TelemetryManager
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.gateway.EmbrGatewayContext
import com.mussonindustrial.embr.gateway.EmbrGatewayContextImpl
import com.mussonindustrial.embr.snmp.agents.SnmpAgentRegistry
import com.mussonindustrial.embr.snmp.agents.rpc.SnmpAgentRpcImpl
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import org.snmp4j.SNMP4JSettings
import org.snmp4j.mp.MPv3
import org.snmp4j.security.SecurityModels
import org.snmp4j.security.SecurityProtocols
import org.snmp4j.security.USM
import org.snmp4j.smi.OctetString

class SnmpGatewayContext(private val context: GatewayContext) :
    EmbrGatewayContext by EmbrGatewayContextImpl(context) {
    companion object {
        lateinit var instance: SnmpGatewayContext

        // Musson Industrial's Private Enterprise Number (PEN)
        // See https://www.iana.org/assignments/enterprise-numbers/ for more information.
        const val PRIVATE_ENTERPRISE_NUMBER = 63707
    }

    val logger = this.getLoggerEx()
    val agentRegistry = SnmpAgentRegistry()
    val agentRpc = SnmpAgentRpcImpl(this)

    init {
        instance = this
        SNMP4JSettings.setEnterpriseID(PRIVATE_ENTERPRISE_NUMBER)
    }

    val securityProtocols: SecurityProtocols =
        SecurityProtocols.getInstance().apply {
            addPredefinedProtocolSet(SecurityProtocols.SecurityProtocolSet.any)
        }
    val engineId: OctetString = OctetString(MPv3.createLocalEngineID())
    val usm = USM(securityProtocols, engineId, 0)

    init {
        logger.trace("Engine ID = $engineId")
        SecurityModels.getInstance().addSecurityModel(usm)
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
