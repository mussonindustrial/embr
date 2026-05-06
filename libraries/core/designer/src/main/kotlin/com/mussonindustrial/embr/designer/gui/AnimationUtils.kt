package com.mussonindustrial.embr.designer.gui

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities

fun ScheduledExecutorService.animate(
    durationMs: Long,
    periodMs: Long = 16L,
    update: (Float) -> Unit,
    done: () -> Unit,
) {
    val start = System.nanoTime()

    val task =
        object : Runnable {
            lateinit var future: ScheduledFuture<*>

            override fun run() {
                val elapsed = (System.nanoTime() - start) / 1_000_000f
                val t = (elapsed / durationMs).coerceIn(0f, 1f)

                SwingUtilities.invokeLater {
                    update(t)
                    if (t >= 1f) {
                        future.cancel(false)
                        done()
                    }
                }
            }
        }

    task.future = scheduleAtFixedRate(task, 0, periodMs, TimeUnit.MILLISECONDS)
}

fun easeOut(t: Float) = 1 - (1 - t) * (1 - t)

fun easeIn(t: Float) = t * t
