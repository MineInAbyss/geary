package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.EntityType

public interface ArchetypeProvider {
    public val rootArchetype: Archetype
    public val count: Int
//    public fun newArchetype(entityType: EntityType): Archetype
    public fun getArchetype(entityType: EntityType): Archetype
}
