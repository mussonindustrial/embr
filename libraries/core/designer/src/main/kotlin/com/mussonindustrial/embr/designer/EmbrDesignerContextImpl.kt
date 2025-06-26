package com.mussonindustrial.embr.designer

import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.mussonindustrial.embr.common.EmbrCommonContextExtension
import com.mussonindustrial.embr.common.EmbrCommonContextExtensionImpl
import com.mussonindustrial.embr.designer.gui.EmblemButton

open class EmbrDesignerContextImpl(private val context: DesignerContext) :
    EmbrDesignerContext,
    DesignerContext by context,
    EmbrCommonContextExtension by EmbrCommonContextExtensionImpl(context) {

    init {
        if (!isInitialized()) initialize()
    }

    private fun initialize() {
        statusBar.addDisplay(EmblemButton(), 1)
        setInitialized()
    }

    private fun isInitialized(): Boolean {
        return System.getProperty("embr.designer.initialized")?.isNotEmpty() == true
    }

    private fun setInitialized() {
        System.setProperty("embr.designer.initialized", "true")
    }
}
