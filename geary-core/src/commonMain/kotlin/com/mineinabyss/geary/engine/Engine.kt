package com.mineinabyss.geary.engine

import kotlinx.coroutines.CoroutineScope

/**
 * An engine service for running the Geary ECS.
 *
 * Its companion object gets a service via Bukkit as its implementation.
 */
interface Engine : CoroutineScope {

    /** Ticks the entire engine. Implementations may call at different speeds. */
    fun tick(currentTick: Long)
}
