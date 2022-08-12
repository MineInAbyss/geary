package com.mineinabyss.geary.datatypes.maps

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.Record
import com.soywiz.kds.*
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

public class TypeMap {
    private val lock = SynchronizedObject()
    private val map: FastIntMap<Record> = FastIntMap()

    // We don't return nullable record to avoid boxing.
    // Accessing an entity that doesn't exist is indicative of a problem elsewhere and should be made obvious.
    @PublishedApi
    internal fun get(entity: Entity): Record = synchronized(lock) { map[entity.id.toInt()] }
        ?: error("Tried to access components on an entity that no longer exists (${entity.id})")

    @PublishedApi
    internal fun set(entity: Entity, record: Record): Unit = synchronized(lock) {
        if (contains(entity)) error("Tried setting the record of an entity that already exists.")
        map[entity.id.toInt()] = record
    }

    internal fun remove(entity: Entity) = synchronized(lock) {
        map.remove(entity.id.toInt())
    }

    public operator fun contains(entity: Entity): Boolean =
        map.contains(entity.id.toInt())
}
