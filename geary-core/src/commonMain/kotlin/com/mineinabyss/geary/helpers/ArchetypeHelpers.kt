package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.modules.archetypes

fun EntityType.getArchetype(): Archetype =
    archetypes.archetypeProvider.getArchetype(this)
