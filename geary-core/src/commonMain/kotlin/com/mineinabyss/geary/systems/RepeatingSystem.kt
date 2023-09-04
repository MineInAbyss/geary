package com.mineinabyss.geary.systems

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.accessors.AccessorThisRef
import com.mineinabyss.geary.systems.query.Query
import kotlin.time.Duration

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
    val interval: Duration = geary.defaults.repeatingSystemInterval
) : Query(), System {
    override fun onStart() {}

    open fun tickAll() {
        fastForEach(run = { it.tick() })
    }

    protected open fun AccessorThisRef.tick() {}
}
