package com.mineinabyss.geary.datatypes.maps

import com.mineinabyss.geary.datatypes.GearyComponentId
import com.mineinabyss.geary.engine.archetypes.Archetype
import org.koin.core.component.KoinComponent

/**
 * Inlined class that acts as a map of components to archetypes. Uses archetype ids for better performance.
 */
public class CompId2ArchetypeMap : KoinComponent {
    public val inner: MutableMap<Long, Archetype> = mutableMapOf()
    public operator fun get(id: GearyComponentId): Archetype? = inner[id.toLong()]
    public operator fun set(id: GearyComponentId, archetype: Archetype) {
        inner[id.toLong()] = archetype
    }

    public operator fun contains(id: GearyComponentId): Boolean = inner.containsKey(id.toLong())
}
