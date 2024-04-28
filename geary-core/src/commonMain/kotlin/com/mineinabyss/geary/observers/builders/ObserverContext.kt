package com.mineinabyss.geary.observers.builders

import com.mineinabyss.geary.datatypes.Entity

interface ObserverContext {
    val entity: Entity
}

interface ObserverContextWithData<R>: ObserverContext {
    val event: R
}
