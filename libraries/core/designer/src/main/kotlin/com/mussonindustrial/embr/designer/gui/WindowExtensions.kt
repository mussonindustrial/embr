package com.mussonindustrial.embr.designer.gui

import java.awt.Window
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

fun Window.onWindowOpenedOnce(block: (WindowEvent) -> Unit) {
    addWindowListener(
        object : WindowAdapter() {
            override fun windowOpened(e: WindowEvent) {
                removeWindowListener(this)
                block(e)
            }
        }
    )
}
