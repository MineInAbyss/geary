package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.engine.archetypes.Archetype

/**
 * A scope provided to [Accessor]s to cache data per archetype.
 */
public open class ArchetypeCacheScope(
    public val archetype: Archetype,
    public val perArchetypeData: List<List<Any?>>,
)

/**
 * An [ArchetypeCacheScope] with a reference to a specific entity in that archetype.
 * It will be processed by an [Accessor] into a [ResultScope].
 *
 * Note: This extends [ArchetypeCacheScope] to allow accessors to read their per archetype cache when processing data.
 *
 * @see Accessor
 */
public class RawAccessorDataScope(
    archetype: Archetype,
    perArchetypeData: List<List<Any?>>,
    public val row: Int,
) : ArchetypeCacheScope(archetype, perArchetypeData) {
    public val entity: GearyEntity = archetype.getEntity(row)
}
