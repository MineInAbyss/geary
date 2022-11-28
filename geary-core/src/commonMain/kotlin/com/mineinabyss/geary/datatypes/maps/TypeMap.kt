package com.mineinabyss.geary.datatypes.maps

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.Record

public interface TypeMap {
    // We don't return nullable record to avoid boxing.
    // Accessing an entity that doesn't exist is indicative of a problem elsewhere and should be made obvious.
    /** Gets the record of a given entity, or throws an error if the entity id is not active in the engine. */
    public operator fun get(entity: Entity): Record

    /** Updates the record of a given entity */
    public operator fun set(entity: Entity, record: Record)

    /** Removes a record associated with an entity. */
    public fun remove(entity: Entity)

    /** Checks if an entity has a record associated with it. */
    public operator fun contains(entity: Entity): Boolean
}
