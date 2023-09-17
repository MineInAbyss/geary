package com.mineinabyss.geary.datatypes.maps

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.Record
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

class ArrayTypeMap : TypeMap {
    private val lock = SynchronizedObject()
    private val map: ArrayList<Record?> = arrayListOf()

    // We don't return nullable record to avoid boxing.
    // Accessing an entity that doesn't exist is indicative of a problem elsewhere and should be made obvious.
    override fun get(entity: Entity): Record = synchronized(lock) { map[entity.id.toInt()] }
        ?: error("Tried to access components on an entity that no longer exists (${entity.id})")

    override fun set(entity: Entity, record: Record): Unit = synchronized(lock) {
        val id = entity.id.toInt()
        if(map.size == id) {
            map.add(record)
            return@synchronized
        }
        if (contains(entity)) error("Tried setting the record of an entity that already exists.")
        while(map.size <= id) map.add(null)
        map[id] = record
    }

    override fun remove(entity: Entity): Unit = synchronized(lock) {
        map[entity.id.toInt()] = null
    }

    override operator fun contains(entity: Entity): Boolean = synchronized(lock) {
        val id = entity.id.toInt()
        map.size < id && map[id] != null
    }
}
