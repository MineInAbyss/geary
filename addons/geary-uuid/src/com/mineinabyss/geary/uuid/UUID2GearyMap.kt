package com.mineinabyss.geary.uuid

import com.mineinabyss.geary.datatypes.EntityId
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.uuid.Uuid

interface UUID2GearyMap {
    operator fun get(uuid: Uuid): EntityId?

    operator fun set(uuid: Uuid, entity: EntityId): EntityId?

    operator fun contains(uuid: Uuid): Boolean
    fun remove(uuid: Uuid): EntityId?
}

class SimpleUUID2GearyMap : UUID2GearyMap {
    private val map = mutableMapOf<Uuid, Long>()

    override operator fun get(uuid: Uuid): EntityId? =
        map[uuid]?.toULong()

    override operator fun set(uuid: Uuid, entity: EntityId): EntityId? =
        map.put(uuid, entity.toLong())?.toULong()

    override operator fun contains(uuid: Uuid): Boolean = map.containsKey(uuid)

    override fun remove(uuid: Uuid): EntityId? =
        map.remove(uuid)?.toULong()
}


class SynchronizedUUID2GearyMap : UUID2GearyMap {
    private val unsafe = SimpleUUID2GearyMap()
    private val lock = SynchronizedObject()

    override fun get(uuid: Uuid): EntityId? = synchronized(lock) { unsafe[uuid] }
    override fun set(uuid: Uuid, entity: EntityId): EntityId? = synchronized(lock) { unsafe.set(uuid, entity) }
    override fun contains(uuid: Uuid): Boolean = synchronized(lock) { unsafe.contains(uuid) }
    override fun remove(uuid: Uuid): EntityId? = synchronized(lock) { unsafe.remove(uuid) }
}
