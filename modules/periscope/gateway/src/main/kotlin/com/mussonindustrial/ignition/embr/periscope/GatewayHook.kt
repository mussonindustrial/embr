package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.perspective.common.api.ComponentRegistry
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegateRegistry
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.mussonindustrial.ignition.embr.periscope.Meta.SHORT_MODULE_ID
import com.mussonindustrial.ignition.embr.periscope.component.embedding.AdvancedFlexRepeater
import com.mussonindustrial.ignition.embr.periscope.component.embedding.AdvancedFlexRepeaterModelDelegate
import com.mussonindustrial.ignition.embr.periscope.component.embedding.Swiper
import java.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
class GatewayHook : AbstractGatewayModuleHook() {

    private val logger: Logger = LoggerFactory.getLogger(SHORT_MODULE_ID)
    private lateinit var context: GatewayContext
    private lateinit var perspectiveContext: PerspectiveContext
    private lateinit var componentRegistry: ComponentRegistry
    private lateinit var modelDelegateRegistry: ComponentModelDelegateRegistry

    override fun setup(context: GatewayContext) {
        this.context = context
    }

    override fun startup(activationState: LicenseState) {
        logger.info("Embr-Periscope module started.")

        perspectiveContext = PerspectiveContext.get(this.context)
        componentRegistry = perspectiveContext.componentRegistry
        modelDelegateRegistry = perspectiveContext.componentModelDelegateRegistry

        logger.info("Registering components...")
        componentRegistry.registerComponent(AdvancedFlexRepeater.DESCRIPTOR)
        modelDelegateRegistry.register(AdvancedFlexRepeater.COMPONENT_ID) {
            AdvancedFlexRepeaterModelDelegate(it)
        }

        componentRegistry.registerComponent(Swiper.DESCRIPTOR)
    }

    override fun shutdown() {
        logger.info("Shutting down Embr-Periscope module and removing registered components.")
        componentRegistry.removeComponent(AdvancedFlexRepeater.COMPONENT_ID)
        modelDelegateRegistry.remove(AdvancedFlexRepeater.COMPONENT_ID)

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
