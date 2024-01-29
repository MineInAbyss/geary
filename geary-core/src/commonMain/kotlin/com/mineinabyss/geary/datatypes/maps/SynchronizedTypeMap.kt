package com.mineinabyss.geary.datatypes.maps

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.Record
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

class SynchronizedTypeMap(private val map: TypeMap) : TypeMap {
    private val lock = SynchronizedObject()

    override fun get(entity: Entity): Record = synchronized(lock) { map[entity] }
    override fun set(entity: Entity, record: Record) = synchronized(lock) { map[entity] = record }
    override fun remove(entity: Entity) = synchronized(lock) { map.remove(entity) }
    override fun contains(entity: Entity): Boolean = synchronized(lock) { map.contains(entity) }
}
