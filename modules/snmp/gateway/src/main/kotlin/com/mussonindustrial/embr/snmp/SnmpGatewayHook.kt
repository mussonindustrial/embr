package com.mussonindustrial.embr.snmp

import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.common.script.ScriptManager
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider
import com.inductiveautomation.ignition.gateway.config.migration.IdbMigrationStrategy
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.AbstractDeviceModuleHook
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceExtensionPoint
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceProfileConfig
import com.inductiveautomation.ignition.gateway.rpc.GatewayRpcImplementation
import com.mussonindustrial.embr.common.Embr
import com.mussonindustrial.embr.snmp.configuration.extensions.SnmpV1ExtensionPoint
import com.mussonindustrial.embr.snmp.configuration.extensions.SnmpV2cExtensionPoint
import com.mussonindustrial.embr.snmp.configuration.extensions.SnmpV3ExtensionPoint
import java.util.*
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
        return listOf(SnmpV1ExtensionPoint, SnmpV2cExtensionPoint, SnmpV3ExtensionPoint)
    }

    override fun getRecordMigrationStrategies(): List<IdbMigrationStrategy> {
        return listOf(
            SnmpV1ExtensionPoint.recordMigrationStrategy,
            SnmpV2cExtensionPoint.recordMigrationStrategy,
            SnmpV3ExtensionPoint.recordMigrationStrategy,
        )
    }

    override fun isFreeModule(): Boolean {
        return true
    }

    override fun isMakerEditionCompatible(): Boolean {
        return true
    }

    override fun initializeScriptManager(manager: ScriptManager) {
        super.initializeScriptManager(manager)

        manager.addScriptModule(
            "system.embr.snmp",
            GatewayScriptModule(),
            PropertiesFileDocProvider(),
        )
    }

    override fun getRpcImplementation(): Optional<GatewayRpcImplementation> {
        return Optional.of(
            GatewayRpcImplementation.of(RpcFunctions.SERIALIZER, RpcFunctionsImpl(context))
        )
    }
}
