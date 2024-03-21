package com.mineinabyss.geary.datatypes.maps

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.engine.archetypes.Archetype

interface TypeMap {
    // We don't return nullable record to avoid boxing.
    // Accessing an entity that doesn't exist is indicative of a problem elsewhere and should be made obvious.
    /** Gets the record of a given entity, or throws an error if the entity id is not active in the engine. */
//    operator fun get(entity: Entity): Record

    /** Updates the record of a given entity */
    operator fun set(entity: Entity, archetype: Archetype, row: Int)

    /** Removes a record associated with an entity. */
    fun remove(entity: Entity)

    /** Checks if an entity has a record associated with it. */
    operator fun contains(entity: Entity): Boolean
}
