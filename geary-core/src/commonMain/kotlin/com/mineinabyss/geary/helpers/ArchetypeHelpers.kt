package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.modules.ArchetypeEngineModule

fun EntityType.getArchetype(world: ArchetypeEngineModule): Archetype =
    world.write.archetypeProvider.getArchetype(this)
