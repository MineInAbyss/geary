package com.mineinabyss.geary.systems.builders

import com.mineinabyss.geary.engine.Pipeline
import com.mineinabyss.geary.systems.System
import com.mineinabyss.geary.systems.TrackedSystem
import com.mineinabyss.geary.systems.query.CachedQueryRunner
import com.mineinabyss.geary.systems.query.Query
import kotlin.time.Duration

class SystemBuilder<T : Query>(
    val query: T,
    val pipeline: Pipeline,
    val interval: Duration? = null
) {
    fun every(interval: Duration): SystemBuilder<T> {
        return SystemBuilder(query, pipeline, interval)
    }

    inline fun exec(crossinline run: T.() -> Unit): TrackedSystem<*> {
        val onTick: CachedQueryRunner<T>.() -> Unit = { forEach(run) }
        val system = System(query, onTick, interval)
        return pipeline.addSystem(system)
    }

    inline fun <R> defer(crossinline run: T.() -> R): DeferredSystemBuilder<T, R> {
        val onTick: CachedQueryRunner<T>.() -> List<CachedQueryRunner.Deferred<R>> = {
            mapWithEntity { run() }
        }
        val system = DeferredSystemBuilder(this, onTick)
        return system
    }

    fun execOnAll(run: CachedQueryRunner<T>.() -> Unit): TrackedSystem<*> {
        val system = System(query, run, interval)
        return pipeline.addSystem(system)
    }
}
