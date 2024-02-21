package com.mineinabyss.geary.systems

import com.mineinabyss.geary.systems.query.CachedQueryRunner

class TrackedSystem(
    val system: System,
    val runner: CachedQueryRunner<*>
) {
    fun tick() {
        system.onTick(runner)
    }
}
