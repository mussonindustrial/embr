package com.mussonindustrial.ignition.embr.charts

import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.mussonindustrial.ignition.embr.designer.EmbrDesignerContext
import com.mussonindustrial.ignition.embr.designer.EmbrDesignerContextImpl

class ChartsDesignerContext(private val context: DesignerContext):
    EmbrDesignerContext by EmbrDesignerContextImpl(context) {

    companion object {
        lateinit var INSTANCE: ChartsDesignerContext
    }
    init {
        INSTANCE = this
    }

}