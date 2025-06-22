package com.mussonindustrial.ignition.embr.charts

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.mussonindustrial.ignition.embr.charts.Meta.SHORT_MODULE_ID
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
class ChartsDesignerHook : AbstractDesignerModuleHook() {

    private val logger: Logger = LoggerFactory.getLogger(SHORT_MODULE_ID)

    private lateinit var context: ChartsDesignerContext

    override fun startup(context: DesignerContext, activationState: LicenseState) {
        logger.debug("Embr-Charts module started.")
        this.context = ChartsDesignerContext(context)

        logger.debug("Registering components...")
        this.context.registerComponents()
    }

    override fun shutdown() {
        logger.debug("Shutting down Embr-Charts module and removing registered components.")

        logger.debug("Removing components...")
        this.context.removeComponents()
    }
}
