package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.properties.EntityPropertyHolder
import com.mineinabyss.geary.ecs.query.Query

public class EventListenerContainer(
    public val addComponent: MutableSet<Event> = mutableSetOf(),
    public val replaceComponent: MutableSet<Event> = mutableSetOf(),
    public val removeComponent: MutableSet<Event> = mutableSetOf(),
    public val removeEntity: MutableSet<Event> = mutableSetOf(),
)

public class GearyEvent: Query() {
}
