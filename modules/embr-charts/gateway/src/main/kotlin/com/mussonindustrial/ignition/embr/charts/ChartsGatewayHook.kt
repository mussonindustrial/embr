package com.mussonindustrial.ignition.embr.charts

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.perspective.common.api.ComponentRegistry
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegateRegistry
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext

import com.mussonindustrial.ignition.embr.charts.component.chart.ChartJs
import com.mussonindustrial.ignition.embr.common.Embr
import com.mussonindustrial.ignition.embr.common.logging.getLogger
import java.util.*


@Suppress("unused")
class ChartsGatewayHook : AbstractGatewayModuleHook() {

    private val logger = this.getLogger()
    private lateinit var context: ChartsGatewayContext
    private lateinit var perspectiveContext: PerspectiveContext
    private lateinit var componentRegistry: ComponentRegistry
    private lateinit var modelDelegateRegistry: ComponentModelDelegateRegistry


    override fun setup(context: GatewayContext) {
        this.context = ChartsGatewayContext(context)
    }

    override fun startup(activationState: LicenseState) {
        logger.info("Embr-Charts module started.")

        perspectiveContext = PerspectiveContext.get(this.context)
        componentRegistry = perspectiveContext.componentRegistry
        modelDelegateRegistry = perspectiveContext.componentModelDelegateRegistry

        logger.info("Registering components...")
        componentRegistry.registerComponent(ChartJs.DESCRIPTOR)

        context.requireModule(Embr.TAG_STREAM.id) {
            logger.info("Embr-TagStream module found. Registering TagStream components...")
        }
    }

    override fun shutdown() {
        logger.info("Shutting down Embr-Charts module and removing registered components.")
        componentRegistry.removeComponent(ChartJs.COMPONENT_ID)
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