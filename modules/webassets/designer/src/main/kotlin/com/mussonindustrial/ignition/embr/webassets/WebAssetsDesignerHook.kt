package com.mussonindustrial.ignition.embr.webassets

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook
import com.inductiveautomation.ignition.designer.model.DesignerContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
class WebAssetsDesignerHook : AbstractDesignerModuleHook() {

    private val logger: Logger = LoggerFactory.getLogger(Meta.SHORT_MODULE_ID)

    private lateinit var context: WebAssetsDesignerContext

    override fun startup(context: DesignerContext, activationState: LicenseState) {
        logger.debug("Embr-WebAssets module started.")
        this.context = WebAssetsDesignerContext(context)
        Meta.addI18NBundle()
    }

    override fun shutdown() {
        logger.debug("Shutting down Embr-WebAssets module.")
        Meta.removeI18NBundle()
    }
}
