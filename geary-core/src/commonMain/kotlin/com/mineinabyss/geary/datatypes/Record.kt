package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.context.EngineContext
import com.mineinabyss.geary.engine.Engine
import com.mineinabyss.geary.engine.archetypes.Archetype
import kotlinx.atomicfu.locks.SynchronizedObject
import org.koin.core.component.get

public class Record internal constructor(
    archetype: Archetype,
    row: Int
) : SynchronizedObject(), EngineContext {
    public var archetype: Archetype
        internal set
    public var row: Int
        internal set

    init {
        this.archetype = archetype
        this.row = row
    }

    internal val entity: GearyEntity get() = archetype.getEntity(row)

    //TODO multiple receivers
    override val engine: Engine get() = get()

    public operator fun component1(): Archetype = archetype
    public operator fun component2(): Int = row
}
