package com.mussonindustrial.embr.thermo

import com.inductiveautomation.ignition.client.model.ClientContext
import com.inductiveautomation.ignition.common.expressions.ExpressionFunctionManager
import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.common.script.ScriptManager
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider
import com.inductiveautomation.vision.api.client.AbstractClientModuleHook
import com.mussonindustrial.embr.common.logging.getLogger
import com.mussonindustrial.embr.thermo.expressions.IF97ExpressionFunction
import com.mussonindustrial.embr.thermo.scripting.IF97ScriptModuleImpl

@Suppress("unused")
class ThermoClientHook : AbstractClientModuleHook() {
    private val logger = this.getLogger()
    private lateinit var context: ThermoClientContext

    override fun startup(context: ClientContext, activationState: LicenseState) {
        logger.info("Embr Thermodynamics module started.")
        this.context = ThermoClientContext(context)
    }

    override fun shutdown() {
        logger.info("Shutting down Embr Thermodynamics module.")
    }

    override fun initializeScriptManager(manager: ScriptManager) {
        manager.addScriptModule(
            IF97ScriptModuleImpl.PATH,
            IF97ScriptModuleImpl(),
            PropertiesFileDocProvider(),
        )
    }

    override fun configureFunctionFactory(factory: ExpressionFunctionManager) {
        factory.categories.add(IF97ExpressionFunction.CATEGORY)
        factory.addFunction(
            IF97ExpressionFunction.NAME,
            IF97ExpressionFunction.CATEGORY,
            IF97ExpressionFunction(),
        )
        super.configureFunctionFactory(factory)
    }
}
