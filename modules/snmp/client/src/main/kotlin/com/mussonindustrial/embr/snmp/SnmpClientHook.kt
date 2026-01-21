package com.mussonindustrial.embr.snmp

import com.inductiveautomation.ignition.client.model.ClientContext
import com.inductiveautomation.ignition.common.expressions.ExpressionFunctionManager
import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.common.script.ScriptManager
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider
import com.inductiveautomation.vision.api.client.AbstractClientModuleHook
import com.mussonindustrial.embr.common.Embr
import com.mussonindustrial.embr.common.scripting.asPyScriptExecutor
import com.mussonindustrial.embr.snmp.agents.expressions.SnmpAgentExpressionFunctions
import com.mussonindustrial.embr.snmp.agents.scripting.SnmpAgentClientScriptModule
import com.mussonindustrial.embr.snmp.agents.scripting.SnmpAgentScriptModule
import com.mussonindustrial.embr.snmp.agents.scripting.SnmpAgentScriptModule.RpcDelegateMethods
import com.mussonindustrial.embr.snmp.scripting.SnmpClientScriptMethodExecutor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
class SnmpClientHook : AbstractClientModuleHook() {

    private val logger: Logger = LoggerFactory.getLogger(Embr.SNMP.shortId)
    private lateinit var context: SnmpClientContext

    override fun startup(context: ClientContext, activationState: LicenseState) {
        logger.debug("Embr-Snmp module startup.")
        this.context = SnmpClientContext(context)
    }

    override fun shutdown() {
        logger.debug("Embr-Snmp module shutdown.")
    }

    override fun initializeScriptManager(scriptManager: ScriptManager) {
        scriptManager.addScriptModule(
            SnmpAgentScriptModule.PATH,
            SnmpAgentClientScriptModule(context.rpc, scriptManager),
            PropertiesFileDocProvider(),
        )
    }

    override fun configureFunctionFactory(factory: ExpressionFunctionManager) {
        SnmpAgentExpressionFunctions(
                RpcDelegateMethods(context.rpc),
                SnmpClientScriptMethodExecutor(context.scriptManager.asPyScriptExecutor()),
            )
            .configureFactory(factory)
        super.configureFunctionFactory(factory)
    }
}
