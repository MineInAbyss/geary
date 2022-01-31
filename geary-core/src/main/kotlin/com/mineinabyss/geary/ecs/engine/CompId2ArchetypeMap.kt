package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.engine.Engine
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import org.koin.core.component.KoinComponent

/**
 * Inlined class that acts as a map of components to archetypes. Uses archetype ids for better performance.
 */
public class CompId2ArchetypeMap(private val engine: Engine) : KoinComponent {
    public val inner: Long2IntOpenHashMap = Long2IntOpenHashMap()
    public operator fun get(id: GearyComponentId): Archetype = engine.getArchetype(inner.get(id.toLong()))
    public operator fun set(id: GearyComponentId, archetype: Archetype) {
        inner.put(id.toLong(), archetype.id)
    }

    public operator fun contains(id: GearyComponentId): Boolean = inner.containsKey(id.toLong())
}
