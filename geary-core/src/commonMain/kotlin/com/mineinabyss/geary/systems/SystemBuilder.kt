package com.mineinabyss.geary.systems

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.systems.query.CachedQueryRunner
import com.mineinabyss.geary.systems.query.Query
import kotlin.time.Duration


inline fun <T : Query> GearyModule.system(
    query: T,
    init: SystemBuilder<T>.() -> Unit
): TrackedSystem {
    val system = SystemBuilder(query).apply(init).build()
    return pipeline.addSystem(system)
}

class SystemBuilder<T : Query>(val query: T) {
    @PublishedApi
    internal var onTick: CachedQueryRunner<T>.() -> Unit = {}
    var interval: Duration = Duration.ZERO

    inline fun onTick(crossinline run: T.() -> Unit) {
        onTick = { forEach(run) }
    }

    fun onTickAll(run: CachedQueryRunner<T>.() -> Unit) {
        onTick = run
    }

    fun build(): System = System(
        query,
        onTick as CachedQueryRunner<*>.() -> Unit,
        interval
    )
}
