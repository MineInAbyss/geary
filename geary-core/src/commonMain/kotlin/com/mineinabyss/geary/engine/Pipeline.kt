package com.mineinabyss.geary.engine

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.System
import com.mineinabyss.geary.systems.TrackedSystem

interface Pipeline {
    fun runOnOrAfter(phase: GearyPhase, block: () -> Unit)
    fun interceptSystemAddition(run: (System) -> System?)
    fun runStartupTasks()

    /** Adds a [system] to the engine, which will be ticked appropriately by the engine. */
    fun addSystem(system: System): TrackedSystem

    fun addSystems(vararg systems: System)

    fun addListener(listener: Listener<*>): Listener<*>

    /** Gets all registered systems in the order they should be executed during an engine tick. */
    fun getRepeatingInExecutionOrder(): Iterable<TrackedSystem>
}
