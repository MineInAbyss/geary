package com.mineinabyss.geary.datatypes.maps

import com.mineinabyss.geary.datatypes.GearyComponentId
import com.mineinabyss.geary.engine.archetypes.Archetype

/**
 * Inlined class that acts as a map of components to archetypes. Uses archetype ids for better performance.
 */
class CompId2ArchetypeMap {
    val inner: MutableMap<Long, Archetype> = mutableMapOf()
    operator fun get(id: GearyComponentId): Archetype? = inner[id.toLong()]
    operator fun set(id: GearyComponentId, archetype: Archetype) {
        inner[id.toLong()] = archetype
    }

    operator fun contains(id: GearyComponentId): Boolean = inner.containsKey(id.toLong())
}
