package com.mineinabyss.geary.events

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.events.queries.Observer

interface EventRunner {
    fun addObserver(observer: Observer)

    /** Calls an event on [target] with data in an [event] entity, optionally with a [source] entity. */
    fun callEvent(
        eventType: ComponentId,
        eventData: Any?,
        involvedComponent: ComponentId?,
        entity: Entity,
    )
}
