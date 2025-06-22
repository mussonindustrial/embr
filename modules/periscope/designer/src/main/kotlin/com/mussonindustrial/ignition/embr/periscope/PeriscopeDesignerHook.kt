package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.perspective.common.PerspectiveModule
import com.inductiveautomation.perspective.designer.DesignerComponentRegistry
import com.inductiveautomation.perspective.designer.api.PerspectiveDesignerInterface
import com.mussonindustrial.embr.perspective.common.component.addResourcesTo
import com.mussonindustrial.embr.perspective.common.component.removeResourcesFrom
import com.mussonindustrial.ignition.embr.periscope.Meta.SHORT_MODULE_ID
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
class PeriscopeDesignerHook : AbstractDesignerModuleHook() {

    private val logger: Logger = LoggerFactory.getLogger(SHORT_MODULE_ID)

    private lateinit var context: PeriscopeDesignerContext
    private lateinit var componentRegistry: DesignerComponentRegistry

    override fun startup(context: DesignerContext, activationState: LicenseState) {
        logger.debug("Embr-Periscope module started.")
        this.context = PeriscopeDesignerContext(context)
        Meta.addI18NBundle()

        val pdi: PerspectiveDesignerInterface = PerspectiveDesignerInterface.get(context)

        componentRegistry = pdi.designerComponentRegistry

        logger.debug("Injecting required resources...")
        componentRegistry.addResourcesTo(PeriscopeComponents.REQUIRED_RESOURCES) {
            it.moduleId() == PerspectiveModule.MODULE_ID
        }

        logger.debug("Registering components...")
        this.context.registerComponents()
    }

    override fun shutdown() {
        logger.debug("Shutting down Embr-Periscope module and removing registered components.")
        Meta.removeI18NBundle()

        logger.debug("Removing injected resources...")
        componentRegistry.removeResourcesFrom(PeriscopeComponents.REQUIRED_RESOURCES) {
            it.moduleId() == PerspectiveModule.MODULE_ID
        }

        logger.debug("Removing components...")
        this.context.removeComponents()
    }
}
