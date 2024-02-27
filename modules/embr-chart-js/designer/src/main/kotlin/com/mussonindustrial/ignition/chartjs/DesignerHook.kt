package com.mussonindustrial.ignition.chartjs

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.perspective.designer.DesignerComponentRegistry
import com.inductiveautomation.perspective.designer.api.ComponentDesignDelegateRegistry
import com.inductiveautomation.perspective.designer.api.PerspectiveDesignerInterface
import com.mussonindustrial.ignition.chartjs.component.display.ChartJs
import com.mussonindustrial.ignition.chartjs.component.display.TagHistoryChart
import org.slf4j.Logger
import org.slf4j.LoggerFactory


@Suppress("unused")
class DesignerHook : AbstractDesignerModuleHook() {

    private val logger: Logger = LoggerFactory.getLogger("Chartjs")

    private lateinit var context: DesignerContext
    private lateinit var componentRegistry: DesignerComponentRegistry
    private lateinit var delegateRegistry: ComponentDesignDelegateRegistry


    override fun startup(context: DesignerContext, activationState: LicenseState) {
        logger.info("Chart.js module started.")
        this.context = context

        val pdi: PerspectiveDesignerInterface = PerspectiveDesignerInterface.get(context)

        componentRegistry = pdi.designerComponentRegistry
        delegateRegistry = pdi.componentDesignDelegateRegistry

        componentRegistry.registerComponent(ChartJs.DESCRIPTOR)
        componentRegistry.registerComponent(TagHistoryChart.DESCRIPTOR)
    }

    override fun shutdown() {
        logger.info("Shutting down Chart.js module and removing registered components.")
        componentRegistry.removeComponent(ChartJs.COMPONENT_ID)
        componentRegistry.removeComponent(TagHistoryChart.COMPONENT_ID)
    }
}