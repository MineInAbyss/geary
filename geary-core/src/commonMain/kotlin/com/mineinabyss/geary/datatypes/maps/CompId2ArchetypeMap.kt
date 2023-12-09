package com.mineinabyss.geary.datatypes.maps

import com.mineinabyss.geary.datatypes.GearyComponentId
import com.mineinabyss.geary.engine.archetypes.Archetype

/**
 * Inlined class that acts as a map of components to archetypes. Uses archetype ids for better performance.
 */
expect class CompId2ArchetypeMap() {
    operator fun get(id: GearyComponentId): Archetype?
    operator fun set(id: GearyComponentId, archetype: Archetype)

    operator fun contains(id: GearyComponentId): Boolean

    fun getOrSet(id: GearyComponentId, put: () -> Archetype): Archetype
}

class CompId2ArchetypeMapViaMutableMap {
    val inner: MutableMap<ULong, Archetype> = mutableMapOf()
    operator fun get(id: GearyComponentId): Archetype? = inner[id]
    operator fun set(id: GearyComponentId, archetype: Archetype) {
        inner[id] = archetype
    }

    operator fun contains(id: GearyComponentId): Boolean = inner.containsKey(id)

    fun getOrSet(id: GearyComponentId, put: () -> Archetype): Archetype {
        return inner[id] ?: put().also { inner[id] = it }
    }
}
