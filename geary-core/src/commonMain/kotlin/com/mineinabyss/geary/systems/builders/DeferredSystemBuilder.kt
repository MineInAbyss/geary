package com.mineinabyss.geary.systems.builders

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.systems.System
import com.mineinabyss.geary.systems.TrackedSystem
import com.mineinabyss.geary.systems.query.CachedQueryRunner
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.geary.systems.query.execOnFinish

class DeferredSystemBuilder<T : Query, R>(
    val systemBuilder: SystemBuilder<T>,
    val mapping: CachedQueryRunner<T>.() -> List<CachedQueryRunner.Deferred<R>>
) {
    inline fun onFinish(crossinline run: (data: R, entity: GearyEntity) -> Unit): TrackedSystem<*> {
        val onTick: CachedQueryRunner<T>.() -> Unit = {
            mapping().execOnFinish(run)
        }
        val system = System(systemBuilder.query, onTick, systemBuilder.interval)
        return systemBuilder.pipeline.addSystem(system)
    }
}
