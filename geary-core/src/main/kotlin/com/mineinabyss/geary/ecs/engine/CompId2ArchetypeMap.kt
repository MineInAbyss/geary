package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.engine.Engine
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * Inlined class that acts as a map of components to archetypes. Uses archetype ids for better performance.
 */
@JvmInline
public value class CompId2ArchetypeMap(public val inner: Long2IntOpenHashMap = Long2IntOpenHashMap()) : KoinComponent {
    public operator fun get(id: GearyComponentId): Archetype = get<Engine>().getArchetype(inner[id.toLong()])
    public operator fun set(id: GearyComponentId, archetype: Archetype) {
        inner[id.toLong()] = archetype.id
    }

    public operator fun contains(id: GearyComponentId): Boolean = inner.containsKey(id.toLong())
}
