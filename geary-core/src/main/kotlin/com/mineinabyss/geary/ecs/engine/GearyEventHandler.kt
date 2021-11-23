package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.accessors.AccessorHolder
import com.mineinabyss.geary.ecs.accessors.RawAccessorDataScope
import com.mineinabyss.geary.ecs.accessors.ResultScope

public class GearyEventHandler<T : Any>(
    public val holder: AccessorHolder,
    public val onEvent: ResultScope.(T) -> Unit
) {
    /** Be sure [event] is of the same type as this listener wants! */
    public fun runEvent(event: Any, scope: RawAccessorDataScope) {
        holder.iteratorFor(scope).forEach { dataCombination ->
            ResultScope(scope.entity, dataCombination, scope.archetype.engine).onEvent(event as T)
        }
    }
}
