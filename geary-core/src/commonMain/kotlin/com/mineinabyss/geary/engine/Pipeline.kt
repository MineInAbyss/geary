package com.mineinabyss.geary.engine

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.System

interface Pipeline {
    fun runOnOrAfter(phase: GearyPhase, block: () -> Unit)
    fun interceptSystemAddition(run: (System) -> System?)
    fun runStartupTasks()

    /** Adds a [system] to the engine, which will be ticked appropriately by the engine. */
    fun addSystem(system: System)

    fun addSystems(vararg systems: System)

    /** Gets all registered systems in the order they should be executed during an engine tick. */
    fun getRepeatingInExecutionOrder(): Iterable<RepeatingSystem>
}
