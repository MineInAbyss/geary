package com.mineinabyss.geary.systems.builders

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.systems.query.CachedQueryRunner
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.geary.systems.query.Query


fun <T : Query> GearyModule.cachedQuery(
    query: T,
): CachedQueryRunner<T> {
    return queryManager.trackQuery(query)
}

fun <T : ListenerQuery> GearyModule.listener(
    query: T
): ListenerBuilder<T> {
    return ListenerBuilder(query, pipeline)
}


fun <T : Query> GearyModule.system(
    query: T
): SystemBuilder<T> {
    return SystemBuilder(query, pipeline)
}
