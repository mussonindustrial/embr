package com.mussonindustrial.ignition.embr.periscope.api

import com.inductiveautomation.ignition.common.util.ExecutionQueue
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class ExecutionQueueScheduledRunnable(val block: () -> Unit) {

    private val cancelled = AtomicBoolean(false)

    val isCancelled: Boolean
        get() = cancelled.get()

    fun run() {
        if (!cancelled.get()) {
            block()
        }
    }

    fun cancel() {
        cancelled.set(true)
    }
}

fun ExecutionQueue.schedule(
    executorService: ScheduledExecutorService,
    block: () -> Unit,
    delay: Long,
    unit: TimeUnit
): ExecutionQueueScheduledRunnable {

    val scheduled = ExecutionQueueScheduledRunnable(block)

    if (delay == 0L) {
        this.submit { scheduled.run() }
    } else {
        executorService.schedule(
            {
                if (!scheduled.isCancelled) {
                    this.submit { scheduled.run() }
                }
            },
            delay,
            unit
        )
    }

    return scheduled
}