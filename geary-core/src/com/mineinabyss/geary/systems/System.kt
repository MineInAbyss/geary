package com.mineinabyss.geary.systems

import com.mineinabyss.geary.systems.query.CachedQuery
import com.mineinabyss.geary.systems.query.Query
import kotlin.time.Duration

class System<T: Query> @PublishedApi internal constructor(
    val name: String,
    val query: T,
    val onTick: CachedQuery<T>.() -> Unit,
    val interval: Duration?,
)
