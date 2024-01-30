package com.mineinabyss.geary.uuid

import com.benasher44.uuid.Uuid
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.toGeary
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

interface UUID2GearyMap {
    operator fun get(uuid: Uuid): GearyEntity?

    operator fun set(uuid: Uuid, entity: GearyEntity): GearyEntity?

    operator fun contains(uuid: Uuid): Boolean
    fun remove(uuid: Uuid): GearyEntity?
}

class SimpleUUID2GearyMap : UUID2GearyMap {
    private val map = mutableMapOf<Uuid, Long>()

    override operator fun get(uuid: Uuid): GearyEntity? =
        map[uuid]?.toGeary()

    override operator fun set(uuid: Uuid, entity: GearyEntity): GearyEntity? =
        map.put(uuid, entity.id.toLong())?.toGeary()

    override operator fun contains(uuid: Uuid): Boolean = map.containsKey(uuid)

    override fun remove(uuid: Uuid): GearyEntity? =
        map.remove(uuid)?.toGeary()
}


class SynchronizedUUID2GearyMap : UUID2GearyMap {
    private val unsafe = SimpleUUID2GearyMap()
    private val lock = SynchronizedObject()

    override fun get(uuid: Uuid): GearyEntity? = synchronized(lock) { unsafe[uuid] }
    override fun set(uuid: Uuid, entity: GearyEntity): GearyEntity? = synchronized(lock) { unsafe.set(uuid, entity) }
    override fun contains(uuid: Uuid): Boolean = synchronized(lock) { unsafe.contains(uuid) }
    override fun remove(uuid: Uuid): GearyEntity? = synchronized(lock) { unsafe.remove(uuid) }
}
