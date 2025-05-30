package com.mussonindustrial.embr.snmp

import com.inductiveautomation.ignition.client.model.ClientContext
import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.vision.api.client.AbstractClientModuleHook
import com.mussonindustrial.embr.common.logging.getLogger

@Suppress("unused")
class SnmpClientHook : AbstractClientModuleHook() {
    private val logger = this.getLogger()
    private lateinit var context: SnmpClientContext

    override fun startup(context: ClientContext, activationState: LicenseState) {
        logger.debug("Embr-SNMP module startup.")
        this.context = SnmpClientContext(context)
    }

    override fun shutdown() {
        logger.debug("Embr-SNMP module shutdown.")
    }
}
