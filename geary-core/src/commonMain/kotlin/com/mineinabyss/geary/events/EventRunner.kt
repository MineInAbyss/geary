package com.mineinabyss.geary.events

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.events.queries.Observer

interface EventRunner {
    fun addObserver(observer: Observer)

    fun callEvent(
        eventType: ComponentId,
        eventData: Any?,
        involvedComponent: ComponentId,
        entity: Entity,
    )
}
