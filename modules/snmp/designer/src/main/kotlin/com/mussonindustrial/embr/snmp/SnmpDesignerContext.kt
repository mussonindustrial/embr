package com.mussonindustrial.embr.snmp

import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.mussonindustrial.embr.designer.EmbrDesignerContext
import com.mussonindustrial.embr.designer.EmbrDesignerContextImpl

data class SnmpDesignerContext(val context: DesignerContext) :
    EmbrDesignerContext by EmbrDesignerContextImpl(context) {
    companion object {
        lateinit var instance: SnmpDesignerContext
    }

    init {
        instance = this
    }
}
