package com.mussonindustrial.embr.designer.gui

import com.inductiveautomation.ignition.client.icons.SvgIconUtil
import com.inductiveautomation.ignition.client.util.BrowserLauncher
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel

class EmblemButton : JLabel() {
    init {
        this.icon = SvgIconUtil.getIcon("mussonindustrial_emblem")
        this.toolTipText =
            "<html><b>Embr by Musson Industrial</b><p>This gateway is using Embr, a collection of open-source of modules by Musson Industrial.<br>You can view the documentation by clicking here.<br>Need support? We can help!"

        addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    BrowserLauncher.openURL("https://docs.mussonindustrial.com/")
                }
            }
        )
    }
}
