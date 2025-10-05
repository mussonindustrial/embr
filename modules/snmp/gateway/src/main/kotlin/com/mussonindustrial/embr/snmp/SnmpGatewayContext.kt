package com.mussonindustrial.embr.snmp

import com.inductiveautomation.ignition.common.execution.ExecutionManager
import com.inductiveautomation.ignition.gateway.model.DiagnosticsManager
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.ignition.gateway.model.TelemetryManager
import com.mussonindustrial.embr.gateway.EmbrGatewayContext
import com.mussonindustrial.embr.gateway.EmbrGatewayContextImpl
import com.mussonindustrial.embr.snmp.configuration.records.SnmpV1DeviceRecord
import com.mussonindustrial.embr.snmp.configuration.records.SnmpV2CDeviceRecord
import com.mussonindustrial.embr.snmp.configuration.records.SnmpV3DeviceRecord
import com.mussonindustrial.embr.snmp.configuration.types.SnmpV1DeviceType
import com.mussonindustrial.embr.snmp.configuration.types.SnmpV2CDeviceType
import com.mussonindustrial.embr.snmp.configuration.types.SnmpV3DeviceType
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import org.snmp4j.SNMP4JSettings
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

    val deviceTypes = listOf(SnmpV1DeviceType, SnmpV2CDeviceType, SnmpV3DeviceType)
    private val records =
        listOf(SnmpV1DeviceRecord.META, SnmpV2CDeviceRecord.META, SnmpV3DeviceRecord.META)

    val securityProtocols: SecurityProtocols =
        SecurityProtocols.getInstance().apply {
            addPredefinedProtocolSet(SecurityProtocols.SecurityProtocolSet.any)
        }
    val engineId: OctetString = OctetString.fromHexStringPairs(PRIVATE_ENTERPRISE_NUMBER.toString())
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
    }

    fun updatePersistentRecords() {
        schemaUpdater.updatePersistentRecords(records)
    }

    override fun getTelemetryManager(): TelemetryManager? {
        return super.getTelemetryManager()
    }

    override fun getDiagnosticsManager(): DiagnosticsManager? {
        return super.getDiagnosticsManager()
    }
}
