package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.modules.GearyConfiguration
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.systems.System
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


inline fun <T : Query> GearyModule.system(
    query: T,
    init: SystemBuilder<T>.() -> Unit
): System {
    val system = SystemBuilder(query).apply(init).build()
    return pipeline.addSystem(system)
}

class SystemTick<T : Query> {
    inline fun forEach(run: T.() -> Unit) {

    }
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
        onTick as CachedQueryRunner<*>.() -> Unit ,
        interval
    )
}

fun printStringSystem() = system(object : Query() {
    var string by target.get<String>()
}) {
    onTick {
        println(string)
    }
}

fun main() {
    system(object : Query() {
        var string by target.get<String>().removable()
    }) {
        onTick {
            println(string)
        }
        interval = 2.seconds
    }
}
