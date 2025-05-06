package com.mussonindustrial.ignition.embr.charts

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.mussonindustrial.embr.common.Embr
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
        logger.debug("Embr-Charts module startup.")

        context.ifModule("com.kyvislabs.apexcharts") {
            logger.info(
                """
                    Kyvis-Labs ApexCharts module is installed. 
                    Embr-Charts replaces Kyvis-Labs ApexCharts.
                    You can and should uninstall the Kyvis-Labs ApexCharts module."
                """
                    .trimIndent()
            )
        }

        logger.debug("Registering components...")
        context.registerComponents()
    }

    override fun shutdown() {
        logger.debug("Embr-Charts module shutdown.")

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
