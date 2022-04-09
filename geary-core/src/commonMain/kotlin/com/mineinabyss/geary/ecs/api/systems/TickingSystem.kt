package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.engine.ArchetypeIterator
import com.mineinabyss.geary.ecs.query.Query
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * #### [Guide: Ticking systems](https://wiki.mineinabyss.com/geary/guide/ticking-systems)
 *
 * A system for the ECS that will run every [interval] ticks.
 *
 * @param interval How often to run this system in ticks.
 *
 * @see [ArchetypeIterator]
 */
public abstract class TickingSystem(
    public val interval: Duration = 50.milliseconds // 1 tick in Minecraft
) : Query(), GearySystem {
    protected var iteration: Int = 0
        private set

    override fun onStart() {}

    //TODO better name differentiation between this and tick
    public fun doTick() {
        iteration++
        tick()
    }

    protected open fun tick() {
        forEach(run = { it.tick() })
    }

    protected open fun TargetScope.tick() {}

    protected fun every(iterations: Int): Boolean =
        iteration.mod(iterations) == 0

    protected inline fun <T> every(iterations: Int, run: () -> T): T? {
        if (every(iterations)) return run()
        return null
    }
}
