package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import it.unimi.dsi.fastutil.longs.Long2LongMap
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import kotlinx.coroutines.sync.Mutex

public class TypeMap {
    private val map: Long2LongMap = Long2LongOpenHashMap().apply { defaultReturnValue(-1L) }
    private val mutexes: MutableMap<Long, Mutex> = mutableMapOf()// Long2ObjectOpenHashMap()

    // We don't return nullable record to avoid boxing.
    // Accessing an entity that doesn't exist is indicative of a problem elsewhere and should be made obvious.
    @PublishedApi
    internal fun unsafeGet(entity: GearyEntity): Record {
        val id = map[entity.id.toLong()]
        if (id == -1L) error("Tried to access components on an entity that no longer exists (${entity.id})")
        return Record(id)
    }

    @PublishedApi
    internal fun unsafeSet(entity: GearyEntity, record: Record) {
        map[entity.id.toLong()] = record.id
    }

    internal fun unsafeRemove(entity: GearyEntity) {
        map.remove(entity.id.toLong())
        mutexes.remove(entity.id.toLong())
    }

    internal fun createMutex(entity: GearyEntity): Mutex {
        val id = entity.id.toLong()
        val mutex = Mutex()
        mutexes[id] = mutex
        return mutex
    }

    internal fun getMutex(entity: GearyEntity): Mutex {
        val id = entity.id.toLong()
        return mutexes.get(id) ?: error("Mutex didn't exist for $entity")
    }

    public operator fun contains(entity: GearyEntity): Boolean = map.contains(entity.id.toLong())
}
