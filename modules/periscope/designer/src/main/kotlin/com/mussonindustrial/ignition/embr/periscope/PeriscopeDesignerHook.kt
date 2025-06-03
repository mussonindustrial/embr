package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.common.project.resource.ProjectResourceId
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.perspective.common.PerspectiveModule
import com.inductiveautomation.perspective.designer.DesignerComponentRegistry
import com.inductiveautomation.perspective.designer.api.PerspectiveDesignerInterface
import com.mussonindustrial.embr.perspective.common.component.addResourcesTo
import com.mussonindustrial.embr.perspective.common.component.removeResourcesFrom
import com.mussonindustrial.ignition.embr.periscope.Meta.SHORT_MODULE_ID
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import com.mussonindustrial.ignition.embr.periscope.resources.DesignerClientResourceDescriptor
import javax.swing.Icon
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
        BundleUtil.get().addBundle(Meta.BUNDLE_PREFIX, this::class.java.classLoader, "localization")

        val pdi: PerspectiveDesignerInterface = PerspectiveDesignerInterface.get(context)

        componentRegistry = pdi.designerComponentRegistry

        logger.debug("Injecting required resources...")
        componentRegistry.addResourcesTo(PeriscopeComponents.REQUIRED_RESOURCES) {
            it.moduleId() == PerspectiveModule.MODULE_ID
        }

        logger.debug("Registering components...")
        this.context.registerComponents()

        logger.debug("Registering ClientResource definitions...")
        this.context.registerClientResourceDefinitions()

        logger.debug("Registering resource editors...")
        this.context.registerResourceWorkspaces()
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

    override fun getResourceIcon(id: ProjectResourceId): Icon? {
        val resource = context.project?.getResource(id.resourcePath)?.orElse(null)
        if (resource == null) return null

        val descriptor =
            context.clientResourceManager.getDescriptor(resource)
                as DesignerClientResourceDescriptor
        return descriptor.icon
    }

    override fun getResourceCategoryKey(id: ProjectResourceId): String? {
        if (id.resourceType == ClientResource.type) return "periscope.client-resource.nouns-long"
        return null
    }
}
