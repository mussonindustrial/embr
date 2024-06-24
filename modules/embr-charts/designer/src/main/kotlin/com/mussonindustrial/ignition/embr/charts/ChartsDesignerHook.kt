package com.mussonindustrial.ignition.embr.charts

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.perspective.designer.DesignerComponentRegistry
import com.inductiveautomation.perspective.designer.api.ComponentDesignDelegateRegistry
import com.inductiveautomation.perspective.designer.api.PerspectiveDesignerInterface
import com.mussonindustrial.ignition.embr.charts.component.chart.ChartJs
import com.mussonindustrial.ignition.embr.common.logging.getLogger
import com.mussonindustrial.ignition.embr.perspective.designer.component.asDesignerDescriptor

@Suppress("unused")
class ChartsDesignerHook : AbstractDesignerModuleHook() {

    private val logger = this.getLogger()

    private lateinit var context: ChartsDesignerContext
    private lateinit var componentRegistry: DesignerComponentRegistry
    private lateinit var delegateRegistry: ComponentDesignDelegateRegistry


    override fun startup(context: DesignerContext, activationState: LicenseState) {
        logger.info("Embr-Charts module started.")
        this.context = ChartsDesignerContext(context)

        val pdi: PerspectiveDesignerInterface = PerspectiveDesignerInterface.get(context)

        componentRegistry = pdi.designerComponentRegistry
        delegateRegistry = pdi.componentDesignDelegateRegistry

        componentRegistry.registerComponent(ChartJs.DESCRIPTOR.asDesignerDescriptor())
        this.context.requireModule("test") {

        }
    }

    override fun shutdown() {
        logger.info("Shutting down Embr-Charts module and removing registered components.")
        componentRegistry.removeComponent(ChartJs.COMPONENT_ID)
    }
}