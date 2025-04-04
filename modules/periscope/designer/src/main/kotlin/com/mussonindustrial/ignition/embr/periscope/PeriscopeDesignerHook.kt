package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.perspective.common.PerspectiveModule
import com.inductiveautomation.perspective.designer.DesignerComponentRegistry
import com.inductiveautomation.perspective.designer.DesignerHook
import com.inductiveautomation.perspective.designer.PerspectiveNavNode
import com.inductiveautomation.perspective.designer.api.ComponentDesignDelegateRegistry
import com.inductiveautomation.perspective.designer.api.PerspectiveDesignerInterface
import com.mussonindustrial.embr.common.reflect.getPrivateProperty
import com.mussonindustrial.embr.perspective.common.component.addResourcesTo
import com.mussonindustrial.embr.perspective.common.component.removeResourcesFrom
import com.mussonindustrial.embr.perspective.designer.component.asDesignerDescriptor
import com.mussonindustrial.ignition.embr.periscope.Meta.SHORT_MODULE_ID
import com.mussonindustrial.ignition.embr.periscope.component.embedding.*
import com.mussonindustrial.ignition.embr.periscope.resources.javascript.JavaScriptModuleResourceWorkspace
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
class PeriscopeDesignerHook : AbstractDesignerModuleHook() {

    private val logger: Logger = LoggerFactory.getLogger(SHORT_MODULE_ID)

    private lateinit var context: DesignerContext
    private lateinit var componentRegistry: DesignerComponentRegistry
    private lateinit var delegateRegistry: ComponentDesignDelegateRegistry

    override fun startup(context: DesignerContext, activationState: LicenseState) {
        logger.debug("Embr-Periscope module started.")
        this.context = PeriscopeDesignerContext(context)
        Meta.addI18NBundle()

        val pdi: PerspectiveDesignerInterface = PerspectiveDesignerInterface.get(context)

        componentRegistry = pdi.designerComponentRegistry
        delegateRegistry = pdi.componentDesignDelegateRegistry

        logger.debug("Injecting required resources...")
        componentRegistry.addResourcesTo(PeriscopeComponents.REQUIRED_RESOURCES) {
            it.moduleId() == PerspectiveModule.MODULE_ID
        }

        componentRegistry.registerComponent(EmbeddedView.DESCRIPTOR.asDesignerDescriptor())
        componentRegistry.registerComponent(FlexRepeater.DESCRIPTOR.asDesignerDescriptor())
        componentRegistry.registerComponent(JsonView.DESCRIPTOR.asDesignerDescriptor())
        componentRegistry.registerComponent(Swiper.DESCRIPTOR.asDesignerDescriptor())
        componentRegistry.registerComponent(Portal.DESCRIPTOR.asDesignerDescriptor())

        logger.debug("Registering resource editors...")
        val pdh = DesignerHook.get(context)
        val perspectiveNavNode = pdh.getPrivateProperty("navNode") as PerspectiveNavNode
        //        val rootNode = WebLibraryRootNode(true)
        context.registerResourceWorkspace(
            JavaScriptModuleResourceWorkspace(context, perspectiveNavNode)
        )

        //        perspectiveNavNode.addChild(rootNode)
    }

    override fun shutdown() {
        logger.debug("Shutting down Embr-Periscope module and removing registered components.")
        Meta.removeI18NBundle()

        logger.debug("Removing injected resources...")
        componentRegistry.removeResourcesFrom(PeriscopeComponents.REQUIRED_RESOURCES) {
            it.moduleId() == PerspectiveModule.MODULE_ID
        }

        componentRegistry.removeComponent(EmbeddedView.COMPONENT_ID)
        componentRegistry.removeComponent(FlexRepeater.COMPONENT_ID)
        componentRegistry.removeComponent(JsonView.COMPONENT_ID)
        componentRegistry.removeComponent(Swiper.COMPONENT_ID)
        componentRegistry.removeComponent(Portal.COMPONENT_ID)
    }
}
