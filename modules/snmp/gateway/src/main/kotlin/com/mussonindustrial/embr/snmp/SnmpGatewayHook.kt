package com.mussonindustrial.embr.snmp

import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.expressions.ExpressionFunctionManager
import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.common.rpc.proto.ProtoRpcSerializer
import com.inductiveautomation.ignition.common.script.ScriptManager
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider
import com.inductiveautomation.ignition.gateway.config.migration.IdbMigrationStrategy
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.AbstractDeviceModuleHook
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceExtensionPoint
import com.inductiveautomation.ignition.gateway.rpc.GatewayRpcImplementation
import com.mussonindustrial.embr.common.Embr
import com.mussonindustrial.embr.common.scripting.asPyScriptExecutor
import com.mussonindustrial.embr.snmp.agents.configuration.extensions.SnmpAgentV1ExtensionPoint
import com.mussonindustrial.embr.snmp.agents.configuration.extensions.SnmpAgentV2cExtensionPoint
import com.mussonindustrial.embr.snmp.agents.configuration.extensions.SnmpAgentV3ExtensionPoint
import com.mussonindustrial.embr.snmp.agents.expressions.SnmpAgentExpressionFunctions
import com.mussonindustrial.embr.snmp.agents.rpc.SnmpAgentRpc
import com.mussonindustrial.embr.snmp.agents.scripting.SnmpAgentGatewayScriptModule
import com.mussonindustrial.embr.snmp.agents.scripting.SnmpAgentScriptModule
import com.mussonindustrial.embr.snmp.agents.scripting.SnmpAgentScriptModule.RpcDelegateMethods
import com.mussonindustrial.embr.snmp.scripting.SnmpGatewayScriptMethodExecutor
import java.util.Optional
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
class SnmpGatewayHook : AbstractDeviceModuleHook() {

    private val logger: Logger = LoggerFactory.getLogger(Embr.SNMP.shortId)

    private val snmpContext: SnmpGatewayContext
        get() = this.context as SnmpGatewayContext

    override fun setup(context: GatewayContext) {
        logger.debug("Embr-SNMP module setup.")
        BundleUtil.get().addBundle("Snmp", this::class.java.classLoader, "localization")

        super.setup(SnmpGatewayContext(context))
    }

    override fun startup(activationState: LicenseState) {
        logger.debug("Embr-SNMP module startup.")
        super.startup(activationState)
    }

    override fun shutdown() {
        logger.debug("Embr-SNMP module shutdown.")
        BundleUtil.get().removeBundle("Snmp")

        super.shutdown()
    }

    override fun getDeviceExtensionPoints(): List<DeviceExtensionPoint<*>> {
        return listOf(
            SnmpAgentV1ExtensionPoint,
            SnmpAgentV2cExtensionPoint,
            SnmpAgentV3ExtensionPoint,
        )
    }

    override fun getRecordMigrationStrategies(): List<IdbMigrationStrategy> {
        return listOf(
            SnmpAgentV1ExtensionPoint.recordMigrationStrategy,
            SnmpAgentV2cExtensionPoint.recordMigrationStrategy,
            SnmpAgentV3ExtensionPoint.recordMigrationStrategy,
        )
    }

    override fun getRpcImplementation(): Optional<GatewayRpcImplementation> {
        return Optional.of(
            GatewayRpcImplementation.newBuilder(ProtoRpcSerializer.DEFAULT_INSTANCE)
                .addInterface(snmpContext.agentRpc, SnmpAgentRpc.SERIALIZER)
                .build()
        )
    }

    override fun initializeScriptManager(scriptManager: ScriptManager) {
        scriptManager.addScriptModule(
            SnmpAgentScriptModule.PATH,
            SnmpAgentGatewayScriptModule(snmpContext.agentRpc, scriptManager),
            PropertiesFileDocProvider(),
        )
    }

    override fun configureFunctionFactory(factory: ExpressionFunctionManager) {
        SnmpAgentExpressionFunctions(
                RpcDelegateMethods(snmpContext.agentRpc),
                SnmpGatewayScriptMethodExecutor(snmpContext.scriptManager.asPyScriptExecutor()),
            )
            .configureFactory(factory)
        super.configureFunctionFactory(factory)
    }

    override fun isFreeModule(): Boolean {
        return true
    }

    override fun isMakerEditionCompatible(): Boolean {
        return true
    }
}
