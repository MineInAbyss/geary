package com.mineinabyss.geary.systems

import com.mineinabyss.geary.engine.Pipeline
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.systems.query.CachedQueryRunner
import com.mineinabyss.geary.systems.query.Query
import kotlin.time.Duration


fun <T : Query> GearyModule.system(
    query: T
): SystemBuilder<T> {
    return SystemBuilder(query, pipeline)
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

//    fun onTickAll(run: CachedQueryRunner<T>.() -> Unit) {
//        onTick = run
//    }

}
