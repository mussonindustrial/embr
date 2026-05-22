package com.mussonindustrial.ignition.embr.periscope.resources.editor.components

import java.awt.Color
import java.awt.Font
import javax.swing.JLabel
import javax.swing.JPanel
import net.miginfocom.swing.MigLayout

open class StatusBar : JPanel(MigLayout("ins 4 12 4 12, fillx, gapx 12", "[][grow][]", "center")) {
    protected val stateLabel =
        JLabel().apply {
            font = Font(Font.DIALOG, Font.BOLD, 12)
            foreground = Color.WHITE
        }

    protected val messageLabel =
        JLabel().apply {
            font = Font(Font.DIALOG, Font.PLAIN, 12)
            foreground = Color.WHITE
        }

    init {
        isOpaque = true
        add(stateLabel)
        add(messageLabel, "growx")
    }
}
