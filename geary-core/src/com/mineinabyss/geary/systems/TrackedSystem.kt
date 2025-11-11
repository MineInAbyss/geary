package com.mineinabyss.geary.systems

import com.mineinabyss.geary.systems.query.CachedQuery
import com.mineinabyss.geary.systems.query.Query

class TrackedSystem<T : Query>(
    val system: System<T>,
    val runner: CachedQuery<T>,
): AutoCloseable {
    val closed get() = runner.closed

    fun tick() {
        !closed || error("System is closed")
        system.onTick(runner)
    }

    /**
     * Stops ticking this system and tracking matched entities against it.
     */
    override fun close() {
        runner.query.world.pipeline.removeSystem(this)
    }
}
