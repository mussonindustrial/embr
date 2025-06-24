package com.mussonindustrial.ignition.embr.webassets

import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.mussonindustrial.embr.designer.EmbrDesignerContext
import com.mussonindustrial.embr.designer.EmbrDesignerContextImpl

class WebAssetsDesignerContext(private val context: DesignerContext) :
    EmbrDesignerContext by EmbrDesignerContextImpl(context) {
    companion object {
        lateinit var instance: WebAssetsDesignerContext
    }

    init {
        instance = this
    }
}
