package com.mineinabyss.geary.datatypes.maps

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.Record

public interface TypeMap {
    // We don't return nullable record to avoid boxing.
    // Accessing an entity that doesn't exist is indicative of a problem elsewhere and should be made obvious.
    public operator fun get(entity: Entity): Record

    public operator fun set(entity: Entity, record: Record)
    public fun remove(entity: Entity)

    public operator fun contains(entity: Entity): Boolean
}
