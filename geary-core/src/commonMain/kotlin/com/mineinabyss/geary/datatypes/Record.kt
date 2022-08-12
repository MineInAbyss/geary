package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.engine.archetypes.Archetype
import kotlinx.atomicfu.locks.SynchronizedObject

public class Record internal constructor(
    archetype: Archetype,
    row: Int
) : SynchronizedObject() {
    public var archetype: Archetype
        internal set
    public var row: Int
        internal set

    init {
        this.archetype = archetype
        this.row = row
    }

    internal val entity: Entity get() = archetype.getEntity(row)

    public operator fun component1(): Archetype = archetype
    public operator fun component2(): Int = row
}
