package com.mineinabyss.geary.engine

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.systems.System
import com.mineinabyss.geary.systems.TrackedSystem
import com.mineinabyss.geary.systems.query.Query

interface Pipeline {
    fun runOnOrAfter(phase: GearyPhase, block: () -> Unit)
    fun onSystemAdd(run: (System<*>) -> Unit)
    fun runStartupTasks()

    /** Adds a [system] to the engine, which will be ticked appropriately by the engine. */
    fun <T: Query>  addSystem(system: System<T>): TrackedSystem<*>


    /** Stops ticking [system] and tracking matched entities against it. */
    fun removeSystem(system: TrackedSystem<*>): Boolean

    fun addSystems(vararg systems: System<*>)

    /** Gets all registered systems in the order they should be executed during an engine tick. */
    fun getRepeatingInExecutionOrder(): Iterable<TrackedSystem<*>>
}
