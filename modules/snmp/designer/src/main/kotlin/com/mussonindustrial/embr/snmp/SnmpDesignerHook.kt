package com.mussonindustrial.embr.snmp

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.common.script.ScriptManager
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.mussonindustrial.embr.common.Embr
import com.mussonindustrial.embr.snmp.agents.scripting.SnmpAgentClientScriptModule
import com.mussonindustrial.embr.snmp.agents.scripting.SnmpAgentScriptModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
class SnmpDesignerHook : AbstractDesignerModuleHook() {

    private val logger: Logger = LoggerFactory.getLogger(Embr.SNMP.shortId)
    private lateinit var context: SnmpDesignerContext

    override fun startup(context: DesignerContext, activationState: LicenseState) {
        logger.debug("Embr-Snmp module startup.")
        this.context = SnmpDesignerContext(context)
    }

    override fun shutdown() {
        logger.debug("Embr-Snmp module shutdown.")
    }

    override fun initializeScriptManager(manager: ScriptManager) {
        manager.addScriptModule(
            SnmpAgentScriptModule.PATH,
            SnmpAgentClientScriptModule(context.agentRpc),
            PropertiesFileDocProvider(),
        )
    }
}
