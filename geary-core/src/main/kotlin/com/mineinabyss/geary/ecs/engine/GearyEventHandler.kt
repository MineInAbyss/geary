package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.accessors.AccessorHolder
import com.mineinabyss.geary.ecs.accessors.EventResultScope
import com.mineinabyss.geary.ecs.accessors.RawAccessorDataScope
import com.mineinabyss.geary.ecs.api.entities.GearyEntity

public class GearyEventHandler(
    public val holder: AccessorHolder,
    public val onEvent: EventResultScope.() -> Unit,
) {
    /** Be sure [event] is of the same type as this listener wants! */
    public fun runEvent(event: GearyEntity, scope: RawAccessorDataScope) {
        holder.iteratorFor(scope).forEach { dataCombination ->
            EventResultScope(scope.entity, dataCombination, event).onEvent()
        }
    }
}
