package com.mussonindustrial.ignition.embr.periscope.resources.editor.language

import java.awt.Component
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.SwingUtilities

fun popupDialog(owner: Component, content: JComponent): JDialog {
    return JDialog(SwingUtilities.getWindowAncestor(owner)).apply {
        isUndecorated = true
        contentPane = content
        addWindowListener(
            object : WindowAdapter() {
                override fun windowDeactivated(e: WindowEvent) = dispose()
            },
        )
    }
}
