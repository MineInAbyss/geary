package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.context.globalContext
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.engine.archetypes.ArchetypeEngine
import com.soywiz.kds.IntSet
import com.soywiz.kds.intSetOf

// TODO context to avoid cast
public fun EntityType.getArchetype(): Archetype =
    (globalContext.engine as ArchetypeEngine).archetypeProvider.getArchetype(this)
