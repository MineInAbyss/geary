package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.engine.Engine
import org.koin.core.component.KoinComponent

/**
 * Inlined class that acts as a map of components to archetypes. Uses archetype ids for better performance.
 */
public class CompId2ArchetypeMap(private val engine: Engine) : KoinComponent {
    public val inner: MutableMap<Long, Int> = mutableMapOf()
    public operator fun get(id: GearyComponentId): Archetype = engine.getArchetype(inner[id.toLong()]!!)
    public operator fun set(id: GearyComponentId, archetype: Archetype) {
        inner[id.toLong()] = archetype.id
    }

    public operator fun contains(id: GearyComponentId): Boolean = inner.containsKey(id.toLong())
}
