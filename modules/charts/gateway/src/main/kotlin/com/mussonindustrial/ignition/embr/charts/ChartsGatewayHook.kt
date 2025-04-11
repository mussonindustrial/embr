package com.mussonindustrial.ignition.embr.charts

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.mussonindustrial.embr.common.Embr
import com.mussonindustrial.embr.common.reflect.withContextClassLoaders
import java.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
class ChartsGatewayHook : AbstractGatewayModuleHook() {

    private val logger: Logger = LoggerFactory.getLogger(Embr.CHARTS.shortId)
    private lateinit var context: ChartsGatewayContext

    override fun setup(context: GatewayContext) {
        logger.debug("Embr-Charts module setup.")
        this.context = ChartsGatewayContext(context)
    }

    override fun startup(activationState: LicenseState) {
        logger.debug("Embr-Charts module started.")

        logger.debug("Registering components...")
        withContextClassLoaders(
            this.javaClass.classLoader,
            context.perspectiveContext.javaClass.classLoader,
        ) {
            context.registerComponents()
        }
    }

    override fun shutdown() {
        logger.debug("Shutting down Embr-Charts module and removing registered components.")

        logger.debug("Removing components...")
        context.removeComponents()
    }

    override fun getMountedResourceFolder(): Optional<String> {
        return Optional.of("static")
    }

    override fun getMountPathAlias(): Optional<String> {
        return Optional.of(Embr.CHARTS.shortId)
    }

    override fun isFreeModule(): Boolean {
        return true
    }

    override fun isMakerEditionCompatible(): Boolean {
        return true
    }
}
