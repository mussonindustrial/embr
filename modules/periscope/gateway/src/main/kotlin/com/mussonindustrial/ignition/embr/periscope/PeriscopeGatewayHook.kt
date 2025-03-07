package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.common.script.ScriptManager
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.perspective.common.PerspectiveModule
import com.inductiveautomation.perspective.common.api.ComponentRegistry
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegateRegistry
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.mussonindustrial.embr.perspective.common.component.addResourcesTo
import com.mussonindustrial.embr.perspective.common.component.removeResourcesFrom
import com.mussonindustrial.ignition.embr.periscope.Meta.SHORT_MODULE_ID
import com.mussonindustrial.ignition.embr.periscope.component.embedding.*
import com.mussonindustrial.ignition.embr.periscope.scripting.JavaScriptFunctions
import java.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
class PeriscopeGatewayHook : AbstractGatewayModuleHook() {

    private val logger: Logger = LoggerFactory.getLogger(SHORT_MODULE_ID)
    private lateinit var context: PeriscopeGatewayContext
    private lateinit var perspectiveContext: PerspectiveContext
    private lateinit var componentRegistry: ComponentRegistry
    private lateinit var modelDelegateRegistry: ComponentModelDelegateRegistry

    override fun setup(context: GatewayContext) {
        logger.debug("Embr-Periscope module setup.")
        this.context = PeriscopeGatewayContext(context)
        BundleUtil.get().addBundle(Meta.BUNDLE_PREFIX, this::class.java.classLoader, "localization")
    }

    override fun startup(activationState: LicenseState) {
        logger.debug("Embr-Periscope module startup.")

        perspectiveContext = context.perspectiveContext
        componentRegistry = perspectiveContext.componentRegistry
        modelDelegateRegistry = perspectiveContext.componentModelDelegateRegistry

        logger.debug("Injecting required resources...")
        componentRegistry.addResourcesTo(PeriscopeComponents.REQUIRED_RESOURCES) {
            it.moduleId() == PerspectiveModule.MODULE_ID
        }

        logger.debug("Registering components...")
        componentRegistry.registerComponent(EmbeddedView.DESCRIPTOR)
        modelDelegateRegistry.register(EmbeddedView.COMPONENT_ID) { EmbeddedViewModelDelegate(it) }

        componentRegistry.registerComponent(JsonView.DESCRIPTOR)
        modelDelegateRegistry.register(JsonView.COMPONENT_ID) { JsonViewModelDelegate(it) }

        componentRegistry.registerComponent(FlexRepeater.DESCRIPTOR)
        modelDelegateRegistry.register(FlexRepeater.COMPONENT_ID) { FlexRepeaterModelDelegate(it) }

        componentRegistry.registerComponent(Swiper.DESCRIPTOR)
    }

    override fun shutdown() {
        logger.debug("Embr-Periscope module shutdown.")
        BundleUtil.get().removeBundle(Meta.BUNDLE_PREFIX)

        logger.debug("Removing injected resources...")
        componentRegistry.removeResourcesFrom(PeriscopeComponents.REQUIRED_RESOURCES) {
            it.moduleId() == PerspectiveModule.MODULE_ID
        }

        componentRegistry.removeComponent(EmbeddedView.COMPONENT_ID)
        modelDelegateRegistry.remove(EmbeddedView.COMPONENT_ID)

        componentRegistry.removeComponent(JsonView.COMPONENT_ID)
        modelDelegateRegistry.remove(JsonView.COMPONENT_ID)

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

    override fun initializeScriptManager(manager: ScriptManager) {
        manager.addScriptModule(
            "system.perspective",
            JavaScriptFunctions(this.context),
            PropertiesFileDocProvider(),
        )
    }
}
