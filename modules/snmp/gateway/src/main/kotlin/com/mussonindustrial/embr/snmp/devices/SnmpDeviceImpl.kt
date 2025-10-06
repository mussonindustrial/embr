package com.mussonindustrial.embr.snmp.devices

import com.mussonindustrial.embr.snmp.SnmpGatewayContext
import com.mussonindustrial.embr.snmp.configuration.extensions.SnmpDeviceConfig
import com.mussonindustrial.embr.snmp.opc.DeviceAddressSpace
import com.mussonindustrial.embr.snmp.opc.DiagnosticAddressSpace
import com.mussonindustrial.embr.snmp.opc.OidAddressSpace
import com.mussonindustrial.embr.snmp.requests.OidReadResult
import com.mussonindustrial.embr.snmp.requests.OidWriteResult
import com.mussonindustrial.embr.snmp.requests.toOidReadResult
import com.mussonindustrial.embr.snmp.requests.toOidWriteResult
import com.mussonindustrial.embr.snmp.utils.addLifecycle
import com.mussonindustrial.embr.snmp.utils.createSizeBoundedPDUs
import com.mussonindustrial.embr.snmp.utils.toDataValue
import java.util.concurrent.TimeUnit
import org.eclipse.milo.opcua.sdk.server.AddressSpaceComposite
import org.eclipse.milo.opcua.sdk.server.Lifecycle
import org.eclipse.milo.opcua.sdk.server.LifecycleManager
import org.eclipse.milo.opcua.stack.core.StatusCodes
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode
import org.snmp4j.PDU
import org.snmp4j.smi.OID
import org.snmp4j.smi.VariableBinding

class SnmpDeviceImpl<T : SnmpDeviceConfig>(override val context: SnmpContext<T>) :
    AddressSpaceComposite(context.deviceContext.server), SnmpDevice {

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
            onDataItemsCreated(
                context.deviceContext.subscriptionModel.getDataItems(context.deviceContext.name)
            )
        }
    }

    override fun getStatus(): String {
        return status.toString()
    }

    override fun startup() {
        context.logger.debug("Starting up...")
        lifecycleManager.startup()
    }

    override fun shutdown() {
        context.logger.debug("Shutting down...")
        lifecycleManager.shutdown()
    }

    override fun read(reads: List<VariableBinding>): List<OidReadResult> {
        val results = mutableMapOf<VariableBinding, OidReadResult>()
        val remaining = reads.groupBy { it.oid }.toMutableMap()

        while (remaining.isNotEmpty()) {

            val pdus =
                context.readTarget.createSizeBoundedPDUs(
                    context.pduFactory,
                    remaining.flatMap { it.value },
                ) {
                    type = PDU.GET
                }

            pdus.forEach { pdu ->
                try {
                    val response = context.snmp.send(pdu, context.readTarget).response
                    if (response == null) {
                        context.logger.warn("GET failed: no response.")
                        return reads.map {
                            DataValue(StatusCodes.Bad_CommunicationError).toOidReadResult()
                        }
                    }

                    if (response.errorStatus == 0) {
                        context.logger.trace("GET successful: ${response.variableBindings}")
                        response.variableBindings.forEach { binding ->
                            remaining[binding.oid]?.forEach {
                                results[it] = binding.variable.toDataValue().toOidReadResult()
                            }
                            remaining.remove(binding.oid)
                        }
                    } else {
                        val errorIdx = response.errorIndex
                        if (errorIdx in 1..pdu.size()) {
                            val badOid = pdu.get(errorIdx - 1).oid
                            context.logger.debug(
                                "GET failed at OID: $badOid (index ${errorIdx}), removing and retrying..."
                            )
                            val failedResults = remaining.remove(badOid)
                            failedResults?.forEach {
                                results[it] =
                                    DataValue(StatusCodes.Bad_NodeIdUnknown).toOidReadResult()
                            }
                        } else {
                            context.logger.warn(
                                "GET failed with errorStatusText: ${response.errorStatusText}"
                            )
                            return reads.map {
                                DataValue(StatusCodes.Bad_CommunicationError).toOidReadResult()
                            }
                        }
                    }
                } catch (e: Exception) {
                    context.logger.warn("GET failed with exception", e)
                    return reads.map {
                        DataValue(StatusCodes.Bad_CommunicationError).toOidReadResult()
                    }
                }
            }
        }

        return reads.map { results[it] as OidReadResult }
    }

    override fun write(writes: List<VariableBinding>): List<OidWriteResult> {
        return writes.map {
            if (context.writeTarget == null) {
                return@map StatusCode(StatusCodes.Bad_WriteNotSupported).toOidWriteResult()
            }

            val pdu =
                context.pduFactory.createPDU(context.writeTarget).apply {
                    type = PDU.SET
                    add(it)
                }

            try {
                val response = context.snmp.send(pdu, context.writeTarget).response
                if (response == null) {
                    context.logger.warn("SET failed: no response.")
                    return@map StatusCode(StatusCodes.Bad_CommunicationError).toOidWriteResult()
                }

                if (response.errorStatus == 0) {
                    return@map StatusCode.GOOD.toOidWriteResult()
                } else {
                    return@map StatusCode.BAD.toOidWriteResult()
                }
            } catch (e: Exception) {
                context.logger.warn("SET failed with exception", e)
                return@map StatusCode(StatusCodes.Bad_CommunicationError).toOidWriteResult()
            }
        }
    }

    inner class Healthcheck : Lifecycle {

        private val taskOwner = "healthcheck"
        private val taskName = context.deviceContext.name

        override fun startup() {
            if (!canDoHealthCheck()) {
                context.logger.debug("Health check disabled, skipping scheduling...")
                status = SnmpDevice.Status.UNKNOWN
                return
            }

            context.logger.debug("Starting health check...")
            SnmpGatewayContext.instance.snmpExecutionManager.registerAtFixedRateWithInitialDelay(
                taskOwner,
                taskName,
                this::doHealthcheck,
                context.snmpConfig.healthcheck.frequency ?: 10000,
                TimeUnit.MILLISECONDS,
                1000,
            )
        }

        override fun shutdown() {
            SnmpGatewayContext.instance.snmpExecutionManager.unRegister(taskOwner, taskName)
        }

        fun canDoHealthCheck(): Boolean {
            context.snmpConfig.healthcheck.frequency.let {
                if (it == null || it <= 0) {
                    return false
                }
            }

            context.snmpConfig.healthcheck.oid.let {
                if (it == null || it.isEmpty()) {
                    return false
                }
            }

            return true
        }

        private fun doHealthcheck() {
            context.logger.trace("Starting health check.")
            if (!canDoHealthCheck()) {
                status = SnmpDevice.Status.UNKNOWN
                return
            }

            val response = read(listOf(VariableBinding(OID(context.snmpConfig.healthcheck.oid))))
            val isGood = response.first().value.statusCode.isGood
            context.logger.trace("Health check result: $isGood")

            status =
                if (isGood) {
                    SnmpDevice.Status.CONNECTED
                } else {
                    SnmpDevice.Status.DISCONNECTED
                }
        }
    }
}
