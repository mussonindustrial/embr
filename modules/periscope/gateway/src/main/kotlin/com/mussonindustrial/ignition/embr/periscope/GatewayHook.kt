package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.perspective.common.api.ComponentRegistry
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegateRegistry
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.mussonindustrial.ignition.embr.periscope.Meta.SHORT_MODULE_ID
import com.mussonindustrial.ignition.embr.periscope.component.embedding.*
import java.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
class GatewayHook : AbstractGatewayModuleHook() {

    private val logger: Logger = LoggerFactory.getLogger(SHORT_MODULE_ID)
    private lateinit var context: PeriscopeGatewayContext
    private lateinit var perspectiveContext: PerspectiveContext
    private lateinit var componentRegistry: ComponentRegistry
    private lateinit var modelDelegateRegistry: ComponentModelDelegateRegistry

    override fun setup(context: GatewayContext) {
        this.context = PeriscopeGatewayContext(context)
    }

    override fun startup(activationState: LicenseState) {
        logger.info("Embr-Periscope module started.")

        perspectiveContext = context.perspectiveContext
        componentRegistry = perspectiveContext.componentRegistry
        modelDelegateRegistry = perspectiveContext.componentModelDelegateRegistry

        logger.info("Registering components...")
        componentRegistry.registerComponent(EmbeddedView.DESCRIPTOR)
        modelDelegateRegistry.register(EmbeddedView.COMPONENT_ID) { EmbeddedViewModelDelegate(it) }

        componentRegistry.registerComponent(FlexRepeater.DESCRIPTOR)
        modelDelegateRegistry.register(FlexRepeater.COMPONENT_ID) { FlexRepeaterModelDelegate(it) }

        componentRegistry.registerComponent(Swiper.DESCRIPTOR)
    }

    override fun shutdown() {
        logger.info("Shutting down Embr-Periscope module and removing registered components.")
        componentRegistry.removeComponent(EmbeddedView.COMPONENT_ID)
        modelDelegateRegistry.remove(EmbeddedView.COMPONENT_ID)

        componentRegistry.removeComponent(FlexRepeater.COMPONENT_ID)
        modelDelegateRegistry.remove(FlexRepeater.COMPONENT_ID)

        componentRegistry.removeComponent(Swiper.COMPONENT_ID)
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
