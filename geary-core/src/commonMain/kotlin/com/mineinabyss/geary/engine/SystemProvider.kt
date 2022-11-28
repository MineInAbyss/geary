package com.mineinabyss.geary.engine

import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.System

public interface SystemProvider {
    /** Adds a [system] to the engine, which will be ticked appropriately by the engine. */
    public fun add(system: System)

    /** Gets all registered systems in the order they should be executed during an engine tick. */
    public fun getRepeatingInExecutionOrder(): Iterable<RepeatingSystem>
}
