package com.mineinabyss.geary.datatypes.maps

import co.touchlab.stately.concurrency.Synchronizable
import co.touchlab.stately.concurrency.synchronize
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.engine.archetypes.Archetype

class SynchronizedArrayTypeMap : ArrayTypeMap() {
    private val lock = Synchronizable()

    override fun getArchAndRow(entity: Entity): ULong {
        return lock.synchronize { super.getArchAndRow(entity) }
    }

    override fun set(entity: Entity, archetype: Archetype, row: Int) {
        lock.synchronize { super.set(entity, archetype, row) }
    }

    override fun remove(entity: Entity) = lock.synchronize { super.remove(entity) }
    override fun contains(entity: Entity): Boolean = lock.synchronize { super.contains(entity) }
}
