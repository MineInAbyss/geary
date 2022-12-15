package com.mineinabyss.geary.engine

import kotlin.time.Duration

abstract class TickingEngine : Engine {
    abstract val tickDuration: Duration

    private var started: Boolean = false

    /** Ticks the entire engine. Implementations may call at different speeds. */
    abstract suspend fun tick(currentTick: Long)

    open fun start(): Boolean {
        if (!started) {
            scheduleSystemTicking()
            started = true
            return true
        }
        return false
    }

    abstract fun scheduleSystemTicking()
}
