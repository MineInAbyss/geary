package com.mineinabyss.geary.engine

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.Entity

public interface EntityMutateOperations {
    /**
     * Sets [data] under a [componentId] for an [entity].
     *
     * @param noEvent Whether to fire an [AddedComponent] event.
     */
    public fun setComponentFor(
        entity: Entity,
        componentId: ComponentId,
        data: Component,
        noEvent: Boolean
    )

    /** Adds this [componentId] to the [entity]'s type but doesn't store any data. */
    public fun addComponentFor(entity: Entity, componentId: ComponentId, noEvent: Boolean)

    /** Removes a [componentId] from an [entity] and clears any data previously associated with it. */
    public fun removeComponentFor(entity: Entity, componentId: ComponentId): Boolean

    /** Removes all components from an entity. */
    public fun clearEntity(entity: Entity)
}
