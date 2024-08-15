package com.mussonindustrial.embr.thermo

import com.inductiveautomation.ignition.common.expressions.ExpressionFunctionManager
import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.common.script.ScriptManager
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.mussonindustrial.embr.common.logging.getLogger
import com.mussonindustrial.embr.thermo.expressions.IF97ExpressionFunction
import com.mussonindustrial.embr.thermo.scripting.IF97ScriptModuleImpl
import java.util.Optional

@Suppress("unused")
class ThermoGatewayHook : AbstractGatewayModuleHook() {
    private val logger = this.getLogger()
    private lateinit var context: ThermoGatewayContext

    override fun setup(context: GatewayContext) {
        this.context = ThermoGatewayContext(context)
    }

    override fun startup(activationState: LicenseState) {
        logger.info("Embr Thermodynamics module started.")
    }

    override fun shutdown() {
        logger.info("Shutting down Embr Thermodynamics module.")
    }

    override fun initializeScriptManager(manager: ScriptManager) {
        manager.addScriptModule(IF97ScriptModuleImpl.PATH, IF97ScriptModuleImpl(), PropertiesFileDocProvider())
    }

    override fun configureFunctionFactory(factory: ExpressionFunctionManager) {
        factory.categories.add(IF97ExpressionFunction.CATEGORY)
        factory.addFunction(IF97ExpressionFunction.NAME, IF97ExpressionFunction.CATEGORY, IF97ExpressionFunction())
        super.configureFunctionFactory(factory)
    }

    override fun getMountedResourceFolder(): Optional<String> {
        return Optional.of("static")
    }

    override fun getMountPathAlias(): Optional<String> {
        return Optional.of(Meta.shortId)
    }

    override fun isFreeModule(): Boolean {
        return true
    }

    override fun isMakerEditionCompatible(): Boolean {
        return true
    }
}
