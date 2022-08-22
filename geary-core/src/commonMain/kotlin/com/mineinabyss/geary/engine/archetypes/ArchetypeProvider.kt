package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.EntityType

public interface ArchetypeProvider {
    public val rootArchetype: Archetype
    public fun newArchetype(entityType: EntityType): Archetype
}
