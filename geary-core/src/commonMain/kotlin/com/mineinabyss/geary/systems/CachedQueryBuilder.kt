package com.mineinabyss.geary.systems

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.systems.query.CachedQueryRunner
import com.mineinabyss.geary.systems.query.Query


fun <T : Query> GearyModule.cachedQuery(
    query: T,
): CachedQueryRunner<T> {
    return queryManager.trackQuery(query)
}
