package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.EntityType

interface ArchetypeProvider {
    val rootArchetype: Archetype

    fun getArchetype(entityType: EntityType): Archetype
}
