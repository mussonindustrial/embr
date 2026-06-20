package com.mussonindustrial.ignition.embr.charts

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.mussonindustrial.embr.common.Embr
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
        logger.debug("Embr-Charts module startup.")

        logger.debug("Registering servlets...")
        context.registerServlets()

        logger.debug("Registering module observers...")
        context.registerModuleObservers()

        logger.debug("Registering components...")
        context.registerComponents()
    }

    override fun shutdown() {
        logger.debug("Embr-Charts module shutdown.")

        logger.debug("Removing module observers...")
        context.removeModuleObservers()

        logger.debug("Removing components...")
        context.removeComponents()

        logger.debug("Unregistering servlets...")
        context.unregisterServlets()
    }

    override fun isFreeModule(): Boolean {
        return !Embr.isLicenseRequired()
    }

    override fun isMakerEditionCompatible(): Boolean {
        return true
    }

    override fun mountRouteHandlers(routes: RouteGroup?) {
        super.mountRouteHandlers(routes)
    }
}
