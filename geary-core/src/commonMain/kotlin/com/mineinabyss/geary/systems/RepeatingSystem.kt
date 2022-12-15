package com.mineinabyss.geary.systems

import com.mineinabyss.geary.engine.archetypes.ArchetypeIterator
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.geary.systems.query.GearyQuery
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
abstract class RepeatingSystem(
    val interval: Duration = 50.milliseconds // 1 tick in Minecraft
) : GearyQuery(), GearySystem {
    protected var iteration: Int = 0
        private set

    override fun onStart() {}

    //TODO better name differentiation between this and tick
    fun doTick() {
        iteration++
        tick()
    }

    protected open fun tick() {
        fastForEach(run = { it.tick() })
    }

    protected open fun TargetScope.tick() {}

    protected fun every(iterations: Int): Boolean =
        iteration.mod(iterations) == 0

    protected inline fun <T> every(iterations: Int, run: () -> T): T? {
        if (every(iterations)) return run()
        return null
    }
}
