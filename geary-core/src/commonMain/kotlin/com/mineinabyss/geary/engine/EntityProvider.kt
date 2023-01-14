package com.mineinabyss.geary.engine

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.EntityType

interface EntityProvider {
    /** Creates a new entity. */
    fun create(initialComponents: Collection<Component> = emptyList()): Entity

    /** Removes an entity, freeing up its entity id for later reuse. */
    fun remove(entity: Entity)

    /** Gets an [entity]'s type */
    fun getType(entity: Entity): EntityType
}
