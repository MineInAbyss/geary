package com.mineinabyss.geary.systems

import com.mineinabyss.geary.systems.query.CachedQuery
import com.mineinabyss.geary.systems.query.Query

class TrackedSystem<T: Query>(
    val system: System<T>,
    val runner: CachedQuery<T>
) {
    fun tick() {
        system.onTick(runner)
    }
}
