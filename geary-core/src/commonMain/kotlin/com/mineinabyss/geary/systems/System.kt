package com.mineinabyss.geary.systems

import com.mineinabyss.geary.systems.query.CachedQueryRunner
import com.mineinabyss.geary.systems.query.Query
import kotlin.time.Duration

class System<T: Query> @PublishedApi internal constructor(
    val name: String,
    val query: T,
    val onTick: CachedQueryRunner<T>.() -> Unit,
    val interval: Duration?,
)
