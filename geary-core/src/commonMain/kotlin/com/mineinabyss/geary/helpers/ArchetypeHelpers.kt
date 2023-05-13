package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.modules.ArchetypeEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.engine.archetypes.Archetype

// TODO context to avoid cast
fun EntityType.getArchetype(): Archetype =
    (geary as ArchetypeEngineModule).archetypeProvider.getArchetype(this)
