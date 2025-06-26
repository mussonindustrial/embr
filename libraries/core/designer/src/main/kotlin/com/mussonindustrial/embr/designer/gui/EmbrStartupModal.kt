package com.mussonindustrial.embr.designer.gui

import java.awt.*
import java.util.concurrent.Executors
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.Timer

class EmbrStartupModal : JWindow() {

    private val executor = Executors.newSingleThreadScheduledExecutor()
    private val autocloseDelay = 2000
    private val fadeDuration = 300L

    init {
        isAlwaysOnTop = true
        background = Color(0, 0, 0, 0)
        opacity = 0f

        val icon = loadResourceIcon("/images/startup/modal.png")
        val label = JLabel(icon).apply { isOpaque = false }

        contentPane.add(label)
        pack()
        setLocationRelativeTo(null)

        isVisible = true
        fadeIn {
            Timer(autocloseDelay) { fadeOut() }
                .apply {
                    isRepeats = false
                    start()
                }
        }
    }

    private fun fadeIn(onComplete: () -> Unit) {
        executor.animate(fadeDuration, update = { t -> opacity = easeIn(t) }, done = onComplete)
    }

    private fun fadeOut() {
        executor.animate(
            fadeDuration,
            update = { t -> opacity = 1f - easeIn(t) },
            done = {
                dispose()
                executor.shutdownNow()
            },
        )
    }

    private fun loadResourceIcon(path: String): ImageIcon? {
        return try {
            val res = this::class.java.getResource(path)
            if (res != null) ImageIcon(ImageIO.read(res)) else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
