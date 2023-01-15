package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.engine.archetypes.Archetype

/**
 * A scope provided to [Accessor]s to cache data per archetype.
 */
open class ArchetypeCacheScope(
    val archetype: Archetype,
    val perArchetypeData: List<List<Any?>>,
)

/**
 * An [ArchetypeCacheScope] with a reference to a specific entity in that archetype.
 * It will be processed by an [Accessor] into a [ResultScope].
 *
 * Note: This extends [ArchetypeCacheScope] to allow accessors to read their per archetype cache when processing data.
 *
 * @see Accessor
 */
class RawAccessorDataScope(
    archetype: Archetype,
    perArchetypeData: List<List<Any?>>,
    val row: Int,
) : ArchetypeCacheScope(archetype, perArchetypeData) {
    val entity: Entity = archetype.getEntity(row)
}
