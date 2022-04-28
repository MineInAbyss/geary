package com.mineinabyss.geary.engine

import kotlin.time.Duration

public abstract class TickingEngine : Engine() {
    public abstract val tickDuration: Duration

    private var started: Boolean = false

    /** Ticks the entire engine. Implementations may call at different speeds. */
    //TODO should this be an abstract class and tick be protected?
    public abstract suspend fun tick(currentTick: Long)

    public open fun start(): Boolean {
        if (!started) {
            scheduleSystemTicking()
            started = true
            return true
        }
        return false
    }

    public abstract fun scheduleSystemTicking()
}
