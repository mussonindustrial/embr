package com.mussonindustrial.embr.snmp.agents.devices

import com.inductiveautomation.ignition.common.util.LoggerEx
import com.mussonindustrial.embr.snmp.SnmpGatewayContext
import com.mussonindustrial.embr.snmp.agents.configuration.SnmpAgentConfig
import com.mussonindustrial.embr.snmp.agents.context.ConcurrentOidModel
import com.mussonindustrial.embr.snmp.agents.context.SnmpAgentContext
import com.mussonindustrial.embr.snmp.agents.opc.BrowsableOidModelAddressSpace
import com.mussonindustrial.embr.snmp.agents.opc.DiagnosticAddressSpace
import com.mussonindustrial.embr.snmp.agents.opc.OidAddressSpace
import com.mussonindustrial.embr.snmp.model.BasicOidValue
import com.mussonindustrial.embr.snmp.model.Oid
import com.mussonindustrial.embr.snmp.model.OidValue
import com.mussonindustrial.embr.snmp.model.Snmp4jOid
import com.mussonindustrial.embr.snmp.model.SnmpCommunicationError
import com.mussonindustrial.embr.snmp.model.toOid
import com.mussonindustrial.embr.snmp.model.toSnmp4j
import com.mussonindustrial.embr.snmp.opc.DeviceAddressSpace
import com.mussonindustrial.embr.snmp.utils.createSizeBoundedPDUs
import java.util.concurrent.TimeUnit
import org.eclipse.milo.opcua.sdk.server.AddressSpaceComposite
import org.eclipse.milo.opcua.sdk.server.Lifecycle
import org.eclipse.milo.opcua.sdk.server.LifecycleManager
import org.eclipse.milo.opcua.stack.core.StatusCodes
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode
import org.snmp4j.PDU
import org.snmp4j.smi.Variable
import org.snmp4j.smi.VariableBinding
import org.snmp4j.util.TableUtils
import org.snmp4j.util.TreeUtils

class SnmpAgentDeviceImpl<T : SnmpAgentConfig>(override val context: SnmpAgentContext<T>) :
    AddressSpaceComposite(context.deviceContext.server), SnmpAgentDevice {

    val lifecycleManager = LifecycleManager()

    val logger: LoggerEx = context.logger.createSubLogger(this::class.java)

    override var status: SnmpAgentDevice.Status = SnmpAgentDevice.Status.DISCONNECTED
        private set

    override val model = ConcurrentOidModel(this)

    val healthcheck = Healthcheck()

    val deviceAddressSpace = DeviceAddressSpace(context.deviceContext, this)
    val diagnosticAddressSpace = DiagnosticAddressSpace(this, this)
    val oidAddressSpace = OidAddressSpace(this, this)
    val browsableOidModelAddressSpace = BrowsableOidModelAddressSpace(this, this)

    init {
        lifecycleManager.addLifecycle(context)
        lifecycleManager.addLifecycle(healthcheck)
        lifecycleManager.addLifecycle(deviceAddressSpace)
        lifecycleManager.addLifecycle(diagnosticAddressSpace)
        lifecycleManager.addLifecycle(oidAddressSpace)
        lifecycleManager.addLifecycle(browsableOidModelAddressSpace)
        lifecycleManager.addStartupTask {
            onDataItemsCreated(
                context.deviceContext.subscriptionModel.getDataItems(context.deviceContext.name)
            )
        }
        lifecycleManager.addStartupTask { model.walk(listOf(Snmp4jOid("1"))) }
    }

    val treeUtils = TreeUtils(context.snmp, context.pduFactory)
    val tableUtils = TableUtils(context.snmp, context.pduFactory)

    override fun getStatus(): String {
        return status.toString()
    }

    override fun startup() {
        logger.debug("Starting up...")
        try {
            lifecycleManager.startup()
            SnmpGatewayContext.instance.agentRegistry.register(this)
        } catch (e: Throwable) {
            status = SnmpAgentDevice.Status.FAULTED
            logger.error("Failed to start device [${context.deviceContext.name}]", e)
        }
    }

    override fun shutdown() {
        logger.debug("Shutting down...")
        try {
            lifecycleManager.shutdown()
            SnmpGatewayContext.instance.agentRegistry.unregister(this)
        } catch (e: Throwable) {
            logger.error("Failed to shutdown device [${context.deviceContext.name}]", e)
        }
    }

    override fun read(reads: List<Oid>): List<OidValue<Variable>> {
        val results = mutableMapOf<Oid, OidValue<Variable>>()
        val remaining = reads.groupBy { it }.toMutableMap()

        while (remaining.isNotEmpty()) {

            val pdus =
                context.readTarget.createSizeBoundedPDUs(
                    context.pduFactory,
                    remaining.flatMap { it.value.map { oid -> VariableBinding(oid.toSnmp4j()) } },
                ) {
                    type = PDU.GET
                }

            pdus.forEach { pdu ->
                try {
                    val response = context.snmp.send(pdu, context.readTarget).response
                    if (response == null) {
                        logger.warn("GET failed: no response.")
                        return reads.map { BasicOidValue(it, SnmpCommunicationError) }
                    }

                    if (response.errorStatus == 0) {
                        logger.trace("GET successful: ${response.variableBindings}")
                        response.variableBindings.forEach { binding ->
                            val oid = binding.oid.toOid()
                            remaining[oid]?.forEach {
                                results[it] = BasicOidValue(it, binding.variable)
                            }
                            remaining.remove(oid)
                        }
                    } else {
                        val errorIdx = response.errorIndex
                        if (errorIdx in 1..pdu.size()) {
                            val oid = (pdu.get(errorIdx - 1).oid).toOid()
                            logger.debug(
                                "GET failed at OID: $oid (index ${errorIdx}), removing and retrying..."
                            )
                            val failedResults = remaining.remove(oid)
                            failedResults?.forEach {
                                results[it] = BasicOidValue(it, SnmpCommunicationError)
                            }
                        } else {
                            logger.warn(
                                "GET failed with errorStatusText: ${response.errorStatusText}"
                            )
                            return reads.map { BasicOidValue(it, SnmpCommunicationError) }
                        }
                    }
                } catch (e: Exception) {
                    logger.warn("GET failed with exception", e)
                    return reads.map { BasicOidValue(it, SnmpCommunicationError) }
                }
            }
        }

        return reads.map { results[it]!! }
    }

    override fun write(writes: List<Pair<Oid, Variable>>): List<OidValue<StatusCode>> {
        return writes.map { (oid, value) ->
            if (context.writeTarget == null) {
                return@map BasicOidValue(oid, StatusCode(StatusCodes.Bad_CommunicationError))
            }

            val pdu =
                context.pduFactory.createPDU(context.writeTarget).apply {
                    type = PDU.SET
                    add(VariableBinding(oid.toSnmp4j(), value))
                }

            try {
                val response = context.snmp.send(pdu, context.writeTarget).response
                if (response == null) {
                    logger.warn("SET failed: no response.")
                    return@map BasicOidValue(oid, StatusCode(StatusCodes.Bad_CommunicationError))
                }

                if (response.errorStatus == 0) {
                    return@map BasicOidValue(oid, StatusCode.GOOD)
                } else {
                    logger.warn("SET failed with errorStatusText: ${response.errorStatusText}")
                    if (response.errorStatusText == "Not writable") {
                        return@map BasicOidValue(oid, StatusCode(StatusCodes.Bad_NotWritable))
                    }
                    return@map BasicOidValue(oid, StatusCode.BAD)
                }
            } catch (e: Exception) {
                logger.warn("SET failed with exception", e)
                return@map BasicOidValue(oid, StatusCode(StatusCodes.Bad_CommunicationError))
            }
        }
    }

    override fun walk(roots: List<Oid>): List<OidValue<Variable>> {
        val results = treeUtils.walk(context.readTarget, roots.map { it.toSnmp4j() }.toTypedArray())
        return results.flatMap {
            it.variableBindings?.map { binding ->
                BasicOidValue(binding.oid.toOid(), binding.variable)
            } ?: listOf()
        }
    }

    override fun readTable(
        columns: List<Oid>,
        lowerBoundIndex: Oid?,
        upperBoundIndex: Oid?,
    ): List<List<OidValue<Variable>>> {
        val results =
            tableUtils.getTable(
                context.readTarget,
                columns.map { it.toSnmp4j() }.toTypedArray(),
                lowerBoundIndex?.toSnmp4j(),
                upperBoundIndex?.toSnmp4j(),
            )
        return results.mapNotNull {
            it.columns?.mapNotNull { binding ->
                BasicOidValue(binding.oid.toOid(), binding.variable)
            }
        }
    }

    inner class Healthcheck : Lifecycle {

        private val taskOwner = "healthcheck"
        private val taskName = context.deviceContext.name

        override fun startup() {
            if (!canDoHealthCheck()) {
                logger.debug("Health check disabled, skipping scheduling...")
                status = SnmpAgentDevice.Status.UNKNOWN
                return
            }

            logger.debug("Starting health check...")
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
            logger.trace("Starting health check.")
            if (!canDoHealthCheck()) {
                status = SnmpAgentDevice.Status.UNKNOWN
                return
            }

            val response = read(listOf(Snmp4jOid(context.snmpConfig.healthcheck.oid!!))).first()
            val isGood = response.value != SnmpCommunicationError
            logger.trace("Health check result: $isGood")

            status =
                if (isGood) {
                    SnmpAgentDevice.Status.CONNECTED
                } else {
                    SnmpAgentDevice.Status.DISCONNECTED
                }
        }
    }
}
