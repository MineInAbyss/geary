package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.engine.Engine

public abstract class TickingEngine : Engine {
    private var started: Boolean = false

    /** Ticks the entire engine. Implementations may call at different speeds. */
    //TODO should this be an abstract class and tick be protected?
    public abstract fun tick(currentTick: Long)

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
