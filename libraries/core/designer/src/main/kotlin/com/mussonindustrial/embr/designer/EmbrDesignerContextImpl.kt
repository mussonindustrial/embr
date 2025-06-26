package com.mussonindustrial.embr.designer

import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.mussonindustrial.embr.common.EmbrCommonContextExtension
import com.mussonindustrial.embr.common.EmbrCommonContextExtensionImpl
import com.mussonindustrial.embr.designer.gui.EmbrHelpMenuItem
import com.mussonindustrial.embr.designer.gui.EmbrStartupModal
import com.mussonindustrial.embr.designer.gui.EmbrStatusBarButton
import com.mussonindustrial.embr.designer.gui.onWindowOpenedOnce
import javax.swing.JFrame
import javax.swing.JMenu

open class EmbrDesignerContextImpl(private val context: DesignerContext) :
    EmbrDesignerContext,
    DesignerContext by context,
    EmbrCommonContextExtension by EmbrCommonContextExtensionImpl(context) {

    init {
        if (!isInitialized()) initialize()
    }

    val helpMenu: JMenu?
        get() {
            val frame = context.frame as JFrame
            val jMenuBar = frame.jMenuBar
            for (menuIndex in 0..jMenuBar.menuCount) {
                val menu = jMenuBar.getMenu(menuIndex)
                if (menu.text == "Help") return menu
            }
            return null
        }

    private fun initialize() {
        setInitialized()

        context.frame.onWindowOpenedOnce {
            helpMenu?.insert(EmbrHelpMenuItem(), 1)
            statusBar.addDisplay(EmbrStatusBarButton(this), 1)

            if (unlicensedEmbrModules.isNotEmpty()) {
                EmbrStartupModal()
            }
        }
    }

    private fun isInitialized(): Boolean {
        return System.getProperty("embr.designer.initialized")?.isNotEmpty() == true
    }

    private fun setInitialized() {
        System.setProperty("embr.designer.initialized", "true")
    }
}
