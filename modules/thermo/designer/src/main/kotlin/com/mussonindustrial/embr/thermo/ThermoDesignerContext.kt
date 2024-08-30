package com.mussonindustrial.embr.thermo

import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.mussonindustrial.embr.designer.EmbrDesignerContext
import com.mussonindustrial.embr.designer.EmbrDesignerContextImpl

data class ThermoDesignerContext(val context: DesignerContext) :
    EmbrDesignerContext by EmbrDesignerContextImpl(context) {
    companion object {
        lateinit var instance: ThermoDesignerContext
    }

    init {
        instance = this
    }
}
