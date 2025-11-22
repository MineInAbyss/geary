package com.mineinabyss.geary.systems.builders

import com.mineinabyss.geary.engine.Pipeline
import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.systems.System
import com.mineinabyss.geary.systems.TrackedSystem
import com.mineinabyss.geary.systems.query.CachedQuery
import com.mineinabyss.geary.systems.query.Query
import kotlin.time.Duration

data class SystemBuilder<T : Query>(
    @PublishedApi
    internal val worldScoped: WorldScoped,
    @PublishedApi
    internal val pipeline: Pipeline,
    val name: String,
    val query: T,
    val interval: Duration? = null,
) {
    fun named(name: String): SystemBuilder<T> {
        return copy(name = name)
    }

    fun every(interval: Duration): SystemBuilder<T> {
        return copy(interval = interval)
    }

    inline fun exec(crossinline run: (T) -> Unit): TrackedSystem<*> {
        val onTick: CachedQuery<T>.() -> Unit = { forEach { run(it) } }
        val system = System(name, query, onTick, interval)
        return worldScoped.addCloseable(pipeline.addSystem(system))
    }

    inline fun <R> defer(crossinline run: (T) -> R): DeferredSystemBuilder<T, R> {
        val onTick: CachedQuery<T>.() -> List<CachedQuery.Deferred<R>> = {
            mapWithEntity { run(it) }
        }
        val system = DeferredSystemBuilder(this, onTick)
        return system
    }

    fun execOnAll(run: CachedQuery<T>.() -> Unit): TrackedSystem<*> {
        val system = System(name, query, run, interval)
        return worldScoped.addCloseable(pipeline.addSystem(system))
    }
}
