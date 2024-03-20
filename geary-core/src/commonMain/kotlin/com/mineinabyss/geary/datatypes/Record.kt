package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.engine.archetypes.Archetype
import kotlinx.atomicfu.locks.SynchronizedObject


data class Record @PublishedApi internal constructor(
    val archetype: Archetype,
    val row: Int
) : SynchronizedObject() {
    val entity: Entity get() = archetype.getEntity(row)
}
