package com.mussonindustrial.ignition.embr.charts

import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.perspective.designer.api.PerspectiveDesignerInterface
import com.mussonindustrial.embr.designer.EmbrDesignerContext
import com.mussonindustrial.embr.designer.EmbrDesignerContextImpl
import com.mussonindustrial.embr.perspective.designer.component.asDesignerComponent
import com.mussonindustrial.embr.perspective.designer.component.registerComponent
import com.mussonindustrial.embr.perspective.designer.component.removeComponent
import com.mussonindustrial.ignition.embr.charts.component.chart.ChartJs

class ChartsDesignerContext(private val context: DesignerContext) :
    EmbrDesignerContext by EmbrDesignerContextImpl(context) {
    companion object {
        lateinit var instance: ChartsDesignerContext
    }

    private val perspectiveDesignerInterface: PerspectiveDesignerInterface
    private val components = listOf(ChartJs.asDesignerComponent())

    init {
        instance = this
        perspectiveDesignerInterface = PerspectiveDesignerInterface.get(context)
    }

    fun registerComponents() {
        components.forEach { perspectiveDesignerInterface.registerComponent(it) }
    }

    fun removeComponents() {
        components.forEach { perspectiveDesignerInterface.removeComponent(it) }
    }
}
