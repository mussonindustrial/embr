package com.mussonindustrial.embr.snmp.devices

import com.mussonindustrial.embr.common.logging.getLogger
import com.mussonindustrial.embr.snmp.configuration.settings.SnmpDeviceSettings
import com.mussonindustrial.embr.snmp.opc.DeviceAddressSpace
import com.mussonindustrial.embr.snmp.opc.DiagnosticAddressSpace
import com.mussonindustrial.embr.snmp.opc.OidAddressSpace
import com.mussonindustrial.embr.snmp.requests.OidReadResult
import com.mussonindustrial.embr.snmp.requests.OidWriteResult
import com.mussonindustrial.embr.snmp.requests.toOidReadResult
import com.mussonindustrial.embr.snmp.requests.toOidWriteResult
import com.mussonindustrial.embr.snmp.utils.addLifecycle
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import org.eclipse.milo.opcua.sdk.server.Lifecycle
import org.eclipse.milo.opcua.sdk.server.LifecycleManager
import org.eclipse.milo.opcua.sdk.server.api.AddressSpaceComposite
import org.eclipse.milo.opcua.stack.core.StatusCodes
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant
import org.snmp4j.PDU
import org.snmp4j.smi.OID
import org.snmp4j.smi.VariableBinding

class SnmpDeviceImpl<T : SnmpDeviceSettings>(override val context: SnmpContext<T>) :
    AddressSpaceComposite(context.deviceContext.getServer()), SnmpDevice {

    val logger = this.getLogger()
    val lifecycleManager = LifecycleManager()

    var status: SnmpDevice.Status = SnmpDevice.Status.DISCONNECTED
        private set

    val healthcheck = Healthcheck()

    val deviceAddressSpace = DeviceAddressSpace(this)
    val diagnosticAddressSpace = DiagnosticAddressSpace(this)
    val oidAddressSpace = OidAddressSpace(this)

    init {
        lifecycleManager.addLifecycle(context.snmp)
        lifecycleManager.addLifecycle(healthcheck)
        lifecycleManager.addLifecycle(deviceAddressSpace)
        lifecycleManager.addLifecycle(diagnosticAddressSpace)
        lifecycleManager.addLifecycle(oidAddressSpace)
        lifecycleManager.addStartupTask {
            onDataItemsCreated(context.deviceContext.getSubscriptionModel().getDataItems(getName()))
        }
    }

    override fun getName(): String {
        return context.deviceSettings.name
    }

    override fun getStatus(): String {
        return status.toString()
    }

    override fun getTypeId(): String {
        return context.deviceSettings.type
    }

    override fun startup() {
        logger.debug("Starting up...")
        lifecycleManager.startup()
    }

    override fun shutdown() {
        logger.debug("Shutting down...")
        lifecycleManager.shutdown()
    }

    override fun read(reads: List<VariableBinding>): List<OidReadResult> {
        val remaining =
            reads.map { it.oid to it }.groupBy({ it.first }, { it.second }).toMutableMap()

        val results = mutableMapOf<VariableBinding, OidReadResult>()

        while (remaining.isNotEmpty()) {
            val pdu =
                PDU().apply {
                    type = PDU.GET
                    remaining.flatMap { it.value }.forEach { add(it) }
                }

            try {
                val response = context.snmp.send(pdu, context.target).response
                if (response == null) {
                    logger.warn("GET failed: no response.")
                    return reads.map {
                        DataValue(StatusCodes.Bad_CommunicationError).toOidReadResult()
                    }
                }

                if (response.errorStatus != 0) {
                    val errorIdx = response.errorIndex
                    if (errorIdx in 1..pdu.size()) {
                        val badOid = pdu.get(errorIdx - 1).oid
                        logger.debug(
                            "GET failed at OID: {} (index {}), removing and retrying...",
                            badOid,
                            errorIdx,
                        )
                        val failedResults = remaining.remove(badOid)
                        failedResults?.forEach {
                            results[it] = DataValue(StatusCodes.Bad_NodeIdUnknown).toOidReadResult()
                        }
                    } else {
                        logger.warn("GET failed with invalid errorIndex: $errorIdx")
                        return reads.map {
                            DataValue(StatusCodes.Bad_CommunicationError).toOidReadResult()
                        }
                    }
                } else {
                    logger.trace("GET successful: {}", response.variableBindings)
                    response.variableBindings.forEach { binding ->
                        remaining[binding.oid]?.forEach {
                            results[it] =
                                DataValue(Variant(binding.variable.toString())).toOidReadResult()
                        }
                    }
                    break
                }
            } catch (e: Exception) {
                logger.debug("GET failed with exception", e)
                return reads.map { DataValue(StatusCodes.Bad_CommunicationError).toOidReadResult() }
            }
        }

        return reads.map { results[it] as OidReadResult }
    }

    override fun write(writes: List<VariableBinding>): List<OidWriteResult> {
        return writes.map {
            val pdu =
                PDU().apply {
                    type = PDU.SET
                    add(it)
                }

            try {
                val response = context.snmp.send(pdu, context.target).response
                if (response == null) {
                    logger.warn("SET failed: no response.")
                    StatusCode(StatusCodes.Bad_CommunicationError).toOidWriteResult()
                }

                if (response.errorStatus == 0) {
                    StatusCode.GOOD.toOidWriteResult()
                } else {
                    StatusCode.BAD.toOidWriteResult()
                }
            } catch (e: Exception) {
                logger.debug("GET failed with exception", e)
                StatusCode(StatusCodes.Bad_CommunicationError).toOidWriteResult()
            }
        }
    }

    inner class Healthcheck : Lifecycle {

        private var future: ScheduledFuture<*>? = null

        override fun startup() {
            future?.cancel(true)

            future =
                context.deviceContext
                    .getGatewayContext()
                    .scheduledExecutorService
                    .scheduleWithFixedDelay(
                        this::doHealthcheck,
                        1000,
                        context.snmpSettings.connectionTimeout,
                        TimeUnit.MILLISECONDS,
                    )
        }

        override fun shutdown() {
            future?.cancel(true)
        }

        private fun doHealthcheck() {
            if (context.snmpSettings.healthcheckOid.isBlank()) {
                status = SnmpDevice.Status.CONNECTED
                return
            }

            val response = read(listOf(VariableBinding(OID(context.snmpSettings.healthcheckOid))))
            val isGood = response.first().value.statusCode?.isGood

            status =
                if (isGood == true) {
                    SnmpDevice.Status.CONNECTED
                } else {
                    SnmpDevice.Status.DISCONNECTED
                }
        }
    }
}
