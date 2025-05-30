package com.mussonindustrial.embr.snmp

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.mussonindustrial.embr.common.logging.getLogger

@Suppress("unused")
class SnmpDesignerHook : AbstractDesignerModuleHook() {
    private val logger = this.getLogger()
    private lateinit var context: SnmpDesignerContext

    override fun startup(context: DesignerContext, activationState: LicenseState) {
        logger.debug("Embr-SNMP module startup.")
        this.context = SnmpDesignerContext(context)
    }

    override fun shutdown() {
        logger.debug("Embr-SNMP module shutdown.")
    }
}
