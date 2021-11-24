package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.accessors.ResultScope
import com.mineinabyss.geary.ecs.engine.ArchetypeIterator
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.ecs.query.Query

/**
 * A system for the ECS that will run every [interval] ticks.
 *
 * @param interval How often to run this system in ticks.
 *
 * @see [ArchetypeIterator]
 */
public abstract class TickingSystem(
    public val interval: Long = 1,
    init: (TickingSystem.() -> Unit)? = null, engine: GearyEngine
) : Query(engine), GearySystem {
    protected var iteration: Int = 0
        private set

    //TODO better name differentiation between this and tick
    public fun doTick() {
        iteration++
        tick()

        // If any archetypes get added here while running through the system we dont want those entities to be iterated
        // right now, since they are most likely the entities involved with the current tick. To avoid this and
        // concurrent modifications, we make a copy of the list before iterating.

    }

    protected open fun tick() {
        forEach { it.tick() }
    }

    protected open fun ResultScope.tick() {}

    protected fun every(iterations: Int): Boolean =
        iteration.mod(iterations) == 0

    protected inline fun <T> every(iterations: Int, run: () -> T): T? {
        if (every(iterations)) return run()
        return null
    }

    public inline fun withEach(action: ResultScope.() -> Unit) {
        forEach { result -> result.action() }
    }

    init {
        if (init != null) init()
    }
}
