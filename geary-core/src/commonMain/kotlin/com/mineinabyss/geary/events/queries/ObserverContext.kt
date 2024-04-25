package com.mineinabyss.geary.events.queries

import com.mineinabyss.geary.datatypes.Entity

interface ObserverContext {
    val entity: Entity
}

interface ObserverContextWithData<R>: ObserverContext {
    val event: R
}
