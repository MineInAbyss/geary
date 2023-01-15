package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.EntityType

interface ArchetypeProvider {
    val rootArchetype: Archetype
    val count: Int

    fun getArchetype(entityType: EntityType): Archetype
}
