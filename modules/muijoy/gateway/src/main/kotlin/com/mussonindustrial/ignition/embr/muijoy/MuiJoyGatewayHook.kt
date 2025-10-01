package com.mussonindustrial.ignition.embr.muijoy

import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.mussonindustrial.ignition.embr.muijoy.Meta.SHORT_MODULE_ID
import java.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
class MuiJoyGatewayHook : AbstractGatewayModuleHook() {

    private val logger: Logger = LoggerFactory.getLogger(SHORT_MODULE_ID)
    private lateinit var context: MuiJoyGatewayContext

    override fun setup(context: GatewayContext) {
        logger.debug("Embr-MuiJoy module setup.")
        this.context = MuiJoyGatewayContext(context)
        BundleUtil.get().addBundle(Meta.BUNDLE_PREFIX, this::class.java.classLoader, "localization")
    }

    override fun startup(activationState: LicenseState) {
        logger.debug("Embr-MuiJoy module startup.")

        logger.debug("Injecting required resources...")
        context.injectResources()

        logger.debug("Registering components...")
        context.registerComponents()
    }

    override fun shutdown() {
        logger.debug("Embr-MuiJoy module shutdown.")
        BundleUtil.get().removeBundle(Meta.BUNDLE_PREFIX)

        logger.debug("Removing injected resources...")
        context.removeResources()

        logger.debug("Removing components...")
        context.removeComponents()
    }

    override fun getMountedResourceFolder(): Optional<String> {
        return Optional.of("static")
    }

    override fun getMountPathAlias(): Optional<String> {
        return Optional.of(SHORT_MODULE_ID)
    }

    override fun isFreeModule(): Boolean {
        return true
    }

    override fun isMakerEditionCompatible(): Boolean {
        return true
    }
}
