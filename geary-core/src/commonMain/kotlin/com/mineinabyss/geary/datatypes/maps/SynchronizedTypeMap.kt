package com.mineinabyss.geary.datatypes.maps

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.engine.archetypes.Archetype
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

class SynchronizedArrayTypeMap() : ArrayTypeMap() {
    private val lock = SynchronizedObject()

    override fun getArchAndRow(entity: Entity): ULong {
        return synchronized(lock) { super.getArchAndRow(entity) }
    }
    override fun set(entity: Entity, archetype: Archetype, row: Int) {
        synchronized(lock) { super.set(entity, archetype, row) }
    }

    override fun remove(entity: Entity) = synchronized(lock) { super.remove(entity) }
    override fun contains(entity: Entity): Boolean = synchronized(lock) { super.contains(entity) }
}
