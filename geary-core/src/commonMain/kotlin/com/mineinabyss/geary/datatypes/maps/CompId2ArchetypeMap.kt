package com.mineinabyss.geary.datatypes.maps

import com.mineinabyss.geary.datatypes.GearyComponentId
import com.mineinabyss.geary.engine.archetypes.Archetype

/**
 * Inlined class that acts as a map of components to archetypes. Uses archetype ids for better performance.
 */
expect class CompId2ArchetypeMap() {
    operator fun get(id: GearyComponentId): Archetype?
    operator fun set(id: GearyComponentId, archetype: Archetype)

    fun entries(): Set<Map.Entry<ULong, Archetype>>

    fun clear()

    fun remove(id: GearyComponentId)

    operator fun contains(id: GearyComponentId): Boolean

    fun getOrSet(id: GearyComponentId, put: () -> Archetype): Archetype

    val size: Int
}

class CompId2ArchetypeMapViaMutableMap {
    val inner: MutableMap<ULong, Archetype> = mutableMapOf()
    operator fun get(id: GearyComponentId): Archetype? = inner[id]
    operator fun set(id: GearyComponentId, archetype: Archetype) {
        inner[id] = archetype
    }

    fun entries(): Set<Map.Entry<ULong, Archetype>> = inner.entries

    fun remove(id: GearyComponentId) {
        inner.remove(id)
    }

    fun clear() {
        inner.clear()
    }

    val size: Int get() = inner.size

    operator fun contains(id: GearyComponentId): Boolean = inner.containsKey(id)

    fun getOrSet(id: GearyComponentId, put: () -> Archetype): Archetype {
        return inner[id] ?: put().also { inner[id] = it }
    }
}
