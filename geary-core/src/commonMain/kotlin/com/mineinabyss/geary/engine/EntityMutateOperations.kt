package com.mineinabyss.geary.engine

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.Entity

interface EntityMutateOperations {
    /**
     * Sets [data] under a [componentId] for an [entity].
     *
     * @param noEvent Whether to fire an [AddedComponent] event.
     */
    fun setComponentFor(
        entity: Entity,
        componentId: ComponentId,
        data: Component,
        noEvent: Boolean
    )

    /** Adds this [componentId] to the [entity]'s type but doesn't store any data. */
    fun addComponentFor(entity: Entity, componentId: ComponentId, noEvent: Boolean)

    fun addPrefabFor(entity: Entity, base: Entity)

    /** Removes a [componentId] from an [entity] and clears any data previously associated with it. */
    fun removeComponentFor(entity: Entity, componentId: ComponentId): Boolean

    /** Removes all components from an entity. */
    fun clearEntity(entity: Entity)
}
