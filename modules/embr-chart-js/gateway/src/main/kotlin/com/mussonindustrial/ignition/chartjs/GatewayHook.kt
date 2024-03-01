package com.mussonindustrial.ignition.chartjs

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.perspective.common.api.ComponentRegistry
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegateRegistry
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.mussonindustrial.ignition.chartjs.Meta.SHORT_MODULE_ID
import com.mussonindustrial.ignition.chartjs.component.display.ChartJs
import com.mussonindustrial.ignition.chartjs.component.display.RealtimeChart
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*


@Suppress("unused")
class GatewayHook : AbstractGatewayModuleHook() {

    private val logger: Logger = LoggerFactory.getLogger("Chartjs")
    private lateinit var context: GatewayContext
    private lateinit var perspectiveContext: PerspectiveContext
    private lateinit var componentRegistry: ComponentRegistry
    private lateinit var modelDelegateRegistry: ComponentModelDelegateRegistry


    override fun setup(context: GatewayContext) {
        this.context = context
    }

    override fun startup(activationState: LicenseState) {
        logger.info("Chart.js module started.")

        perspectiveContext = PerspectiveContext.get(this.context)
        componentRegistry = perspectiveContext.componentRegistry
        modelDelegateRegistry = perspectiveContext.componentModelDelegateRegistry

        logger.info("Registering components...")
        componentRegistry.registerComponent(ChartJs.DESCRIPTOR)
        componentRegistry.registerComponent(RealtimeChart.DESCRIPTOR)

        logger.info("Registering model delegates...")
        modelDelegateRegistry.register(ChartJs.COMPONENT_ID, TagHistoryComponentModelDelegateFactory(context))

    }

    override fun shutdown() {
        logger.info("Shutting down Chart.js module and removing registered components.")
        componentRegistry.removeComponent(ChartJs.COMPONENT_ID)
        componentRegistry.removeComponent(RealtimeChart.COMPONENT_ID)
        modelDelegateRegistry.remove(ChartJs.COMPONENT_ID)
    }

    override fun getMountedResourceFolder(): Optional<String>? {
        return Optional.of("static")
    }

    override fun getMountPathAlias(): Optional<String>? {
        return Optional.of(SHORT_MODULE_ID)
    }

    override fun isFreeModule(): Boolean {
        return true
    }

    override fun isMakerEditionCompatible(): Boolean {
        return true
    }
}