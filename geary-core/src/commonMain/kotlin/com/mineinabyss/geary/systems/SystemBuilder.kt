package com.mineinabyss.geary.systems

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.engine.Pipeline
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.systems.query.CachedQueryRunner
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.geary.systems.query.execOnFinish
import kotlin.time.Duration


fun <T : Query> GearyModule.system(
    query: T
): SystemBuilder<T> {
    return SystemBuilder(query, pipeline)
}

class DeferredSystemBuilder<T : Query, R>(
    val systemBuilder: SystemBuilder<T>,
    val mapping: CachedQueryRunner<T>.() -> List<CachedQueryRunner.Deferred<R>>
) {
    inline fun onFinish(crossinline run: (data: R, entity: GearyEntity) -> Unit): TrackedSystem {
        val onTick: CachedQueryRunner<T>.() -> Unit = {
            mapping().execOnFinish(run)
        }
        val system = System(
            systemBuilder.query,
            onTick as CachedQueryRunner<*>.() -> Unit,
            systemBuilder.interval
        )
        return systemBuilder.pipeline.addSystem(system)
    }
}

class SystemBuilder<T : Query>(val query: T, val pipeline: Pipeline) {
    var interval: Duration = Duration.ZERO

    inline fun exec(crossinline run: T.() -> Unit): TrackedSystem {
        val onTick: CachedQueryRunner<T>.() -> Unit = { forEach(run) }
        val system = System(
            query,
            onTick as CachedQueryRunner<*>.() -> Unit,
            interval
        )
        return pipeline.addSystem(system)
    }

    inline fun <R> defer(crossinline run: T.() -> R): DeferredSystemBuilder<T, R> {
        val onTick: CachedQueryRunner<T>.() -> List<CachedQueryRunner.Deferred<R>> = {
            mapWithEntity { run() }
        }
        val system = DeferredSystemBuilder(this, onTick)
        return system
    }

    fun execOnAll(run: CachedQueryRunner<T>.() -> Unit): TrackedSystem {
        val system = System(
            query,
            run as CachedQueryRunner<*>.() -> Unit,
            interval
        )
        return pipeline.addSystem(system)
    }
}
