package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.EntityType

public class SimpleArchetypeProvider: ArchetypeProvider {
    override val rootArchetype: Archetype
        get() = Archetype(this, EntityType(), 0)


    override fun newArchetype(entityType: EntityType): Archetype {
        TODO("Not yet implemented")
    }
}
