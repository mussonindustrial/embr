package com.mussonindustrial.ignition.embr.webassets

import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.mussonindustrial.ignition.embr.webassets.Meta.SHORT_MODULE_ID
import java.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
class WebAssetsGatewayHook : AbstractGatewayModuleHook() {

    private val logger: Logger = LoggerFactory.getLogger(SHORT_MODULE_ID)
    private lateinit var context: WebAssetsGatewayContext

    override fun setup(context: GatewayContext) {
        logger.debug("Embr-WebAssets module setup.")
        this.context = WebAssetsGatewayContext(context)
        BundleUtil.get().addBundle(Meta.BUNDLE_PREFIX, this::class.java.classLoader, "localization")
    }

    override fun startup(activationState: LicenseState) {
        logger.debug("Embr-WebAssets module startup.")

        logger.debug("Registering module observers...")
        context.registerModuleObservers()

        logger.debug("Registering servlets...")
        context.registerServlets()

        logger.debug("Starting folder watchers...")
        context.startWebJarFolderWatcher()
    }

    override fun shutdown() {
        logger.debug("Embr-WebAssets module shutdown.")
        BundleUtil.get().removeBundle(Meta.BUNDLE_PREFIX)

        logger.debug("Removing module observers...")
        context.removeModuleObservers()

        logger.debug("Stopping folder watchers...")
        context.stopWebJarFolderWatcher()
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
