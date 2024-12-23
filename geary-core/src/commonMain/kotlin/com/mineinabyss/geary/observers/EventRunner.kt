package com.mineinabyss.geary.observers

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.EntityId

interface EventRunner {
    fun addObserver(observer: Observer)

    fun callEvent(
        eventType: ComponentId,
        eventData: Any?,
        involvedComponent: ComponentId,
        entity: EntityId,
    )
}
