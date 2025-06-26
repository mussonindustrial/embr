package com.mussonindustrial.embr.designer

import com.inductiveautomation.ignition.client.icons.SvgIconUtil
import com.inductiveautomation.ignition.client.util.BrowserLauncher
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.mussonindustrial.embr.common.EmbrCommonContextExtension
import com.mussonindustrial.embr.common.EmbrCommonContextExtensionImpl
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JLabel

open class EmbrDesignerContextImpl(private val context: DesignerContext) :
    EmbrDesignerContext,
    DesignerContext by context,
    EmbrCommonContextExtension by EmbrCommonContextExtensionImpl(context) {

    companion object {
        private const val DOCUMENTATION_SITE = "https://docs.mussonindustrial.com/"
        private val initialized = AtomicBoolean(false)
    }

    init {
        if (!initialized.getAndSet(true)) {
            initialize()
        }
    }

    private fun initialize() {
        val icon = SvgIconUtil.getIcon("mussonindustrial_emblem")
        val label =
            JLabel(icon).apply {
                toolTipText =
                    "<html><b>Embr by Musson Industrial</b><p>Embr is collection of open-source of modules by Musson Industrial.<br>You can view the documentation by clicking below.</p><br><p>Need support? We can help!</p>"
                addMouseListener(
                    object : MouseAdapter() {
                        override fun mouseClicked(e: MouseEvent) {
                            BrowserLauncher.openURL(DOCUMENTATION_SITE)
                        }
                    }
                )
            }
        statusBar.addDisplay(label, 1)
    }
}
