package com.mineinabyss.geary.datatypes.maps

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.engine.archetypes.Archetype

interface TypeMap {
    /** Updates the record of a given entity */
    operator fun set(entity: Entity, archetype: Archetype, row: Int)

    /** Removes a record associated with an entity. */
    fun remove(entity: Entity)

    /** Checks if an entity has a record associated with it. */
    operator fun contains(entity: Entity): Boolean
}
