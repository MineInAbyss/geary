package com.mineinabyss.geary.events.queries

import com.mineinabyss.geary.datatypes.Entity

data class ObserverContext(
    val entity: Entity
)

data class ObserverContextWithData<R>(
    val entity: Entity,
    val event: R,
)
