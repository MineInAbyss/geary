package com.mineinabyss.geary.systems

import com.mineinabyss.geary.systems.query.CachedQueryRunner
import com.mineinabyss.geary.systems.query.Query
import kotlin.time.Duration

class System internal constructor(
    val query: Query,
    val onTick: CachedQueryRunner<*>.() -> Unit,
    val interval: Duration,
)
