package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.engine.archetypes.Archetype
import kotlinx.atomicfu.locks.SynchronizedObject

class Record @PublishedApi internal constructor(
    archetype: Archetype,
    row: Int
) : SynchronizedObject() {
    var archetype: Archetype
        internal set
    var row: Int
        internal set

    init {
        this.archetype = archetype
        this.row = row
    }

    val entity: Entity get() = archetype.getEntity(row)

    operator fun component1(): Archetype = archetype
    operator fun component2(): Int = row
}
