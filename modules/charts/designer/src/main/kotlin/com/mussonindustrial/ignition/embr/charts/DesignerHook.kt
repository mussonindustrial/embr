package com.mussonindustrial.ignition.embr.charts

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.perspective.designer.DesignerComponentRegistry
import com.inductiveautomation.perspective.designer.api.ComponentDesignDelegateRegistry
import com.inductiveautomation.perspective.designer.api.PerspectiveDesignerInterface
import com.mussonindustrial.ignition.embr.charts.Meta.SHORT_MODULE_ID
import com.mussonindustrial.ignition.embr.charts.component.asDesignerDescriptor
import com.mussonindustrial.ignition.embr.charts.component.chart.ChartJs
import com.mussonindustrial.ignition.embr.charts.component.embedding.Swiper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
class DesignerHook : AbstractDesignerModuleHook() {

    private val logger: Logger = LoggerFactory.getLogger(SHORT_MODULE_ID)

    private lateinit var context: DesignerContext
    private lateinit var componentRegistry: DesignerComponentRegistry
    private lateinit var delegateRegistry: ComponentDesignDelegateRegistry

    override fun startup(context: DesignerContext, activationState: LicenseState) {
        logger.info("Embr-Charts module started.")
        this.context = context

        val pdi: PerspectiveDesignerInterface = PerspectiveDesignerInterface.get(context)

        componentRegistry = pdi.designerComponentRegistry
        delegateRegistry = pdi.componentDesignDelegateRegistry

        componentRegistry.registerComponent(ChartJs.DESCRIPTOR.asDesignerDescriptor())
        componentRegistry.registerComponent(Swiper.DESCRIPTOR.asDesignerDescriptor())
    }

    override fun shutdown() {
        logger.info("Shutting down Embr-Charts module and removing registered components.")
        componentRegistry.removeComponent(ChartJs.COMPONENT_ID)
        componentRegistry.removeComponent(Swiper.COMPONENT_ID)
    }
}
