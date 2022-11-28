package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.context.GearyArchetypeModule
import com.mineinabyss.geary.context.geary
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.engine.archetypes.ArchetypeEngine

// TODO context to avoid cast
public fun EntityType.getArchetype(): Archetype =
    (geary as GearyArchetypeModule).archetypeProvider.getArchetype(this)
