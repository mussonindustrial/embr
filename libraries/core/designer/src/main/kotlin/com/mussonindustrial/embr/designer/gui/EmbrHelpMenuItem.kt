package com.mussonindustrial.embr.designer.gui

import com.inductiveautomation.ignition.client.util.BrowserLauncher
import com.mussonindustrial.embr.common.Embr
import javax.swing.JMenuItem

class EmbrHelpMenuItem : JMenuItem("Embr Help") {
    init {
        toolTipText = "Launch the Embr user manual in a web browser"
        icon = EmbrDesignerIcons.help
        addActionListener { BrowserLauncher.openURL(Embr.DOCUMENTATION_URL) }
    }
}
