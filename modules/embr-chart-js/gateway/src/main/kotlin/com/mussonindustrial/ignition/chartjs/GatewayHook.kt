package com.mussonindustrial.ignition.chartjs

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.perspective.common.api.ComponentRegistry
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegateRegistry
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.mussonindustrial.ignition.chartjs.Meta.SHORT_MODULE_ID
import com.mussonindustrial.ignition.chartjs.component.display.ChartJs
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*


@Suppress("unused")
class GatewayHook : AbstractGatewayModuleHook() {

    private val logger: Logger = LoggerFactory.getLogger("Chartjs")
    private lateinit var gatewayContext: GatewayContext
    private lateinit var perspectiveContext: PerspectiveContext
    private lateinit var componentRegistry: ComponentRegistry
    private lateinit var modelDelegateRegistry: ComponentModelDelegateRegistry


    override fun setup(context: GatewayContext) {
        this.gatewayContext = context
    }

    override fun startup(activationState: LicenseState) {
        logger.info("Chart.js module started.")

        this.perspectiveContext = PerspectiveContext.get(this.gatewayContext)
        this.componentRegistry = perspectiveContext.componentRegistry
        this.modelDelegateRegistry = perspectiveContext.componentModelDelegateRegistry

        logger.info("Registering components...")
        this.componentRegistry.registerComponent(ChartJs.DESCRIPTOR)

        logger.info("Registering model delegates...")
        this.modelDelegateRegistry.register(ChartJs.COMPONENT_ID, ::ChartJsComponentModelDelegate)

    }

    override fun shutdown() {
        logger.info("Shutting down Chart.js module and removing registered components.")
        this.componentRegistry.removeComponent(ChartJs.COMPONENT_ID)
        this.modelDelegateRegistry.remove(ChartJs.COMPONENT_ID)
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