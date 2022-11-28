package com.mineinabyss.geary.engine

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.EntityType

public interface EntityProvider {
    /** Creates a new entity. */
    public fun newEntity(initialComponents: Collection<Component> = emptyList()): Entity

    /** Removes an entity, freeing up its entity id for later reuse. */
    public fun removeEntity(entity: Entity)

    /** Gets an [entity]'s type */
    public fun getType(entity: Entity): EntityType
}
