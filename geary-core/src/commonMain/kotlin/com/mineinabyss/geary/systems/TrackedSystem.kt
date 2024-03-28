package com.mineinabyss.geary.systems

import com.mineinabyss.geary.systems.query.CachedQueryRunner
import com.mineinabyss.geary.systems.query.Query

class TrackedSystem<T: Query>(
    val system: System<T>,
    val runner: CachedQueryRunner<T>
) {
    fun tick() {
        system.onTick(runner)
    }
}
