package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.engine.Engine

public abstract class TickingEngine : Engine {
    private var started: Boolean = false

    /** Ticks the entire engine. Implementations may call at different speeds. */
    //TODO should this be an abstract class and tick be protected?
    public abstract fun tick(currentTick: Long)

    public fun start() {
        if (!started) {
            scheduleSystemTicking()
            started = true
        }
    }

    public abstract fun scheduleSystemTicking()
}
