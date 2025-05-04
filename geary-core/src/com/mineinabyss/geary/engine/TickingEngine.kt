package com.mineinabyss.geary.engine

import kotlin.time.Duration

abstract class TickingEngine : Engine {
    abstract val tickDuration: Duration

    private var started: Boolean = false

    open fun start(): Boolean {
        if (!started) {
            started = true
            return true
        }
        return false
    }
}
