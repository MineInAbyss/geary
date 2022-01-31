package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.ConcurrentHashMap

public class TypeMap {
    private val map: MutableMap<Long, Long> = ConcurrentHashMap<Long, Long>()
    private val mutexes: MutableMap<Long, Mutex> = ConcurrentHashMap<Long, Mutex>()

    // We don't return nullable record to avoid boxing.
    // Accessing an entity that doesn't exist is indicative of a problem elsewhere and should be made obvious.
    @PublishedApi
    internal fun unsafeGet(entity: GearyEntity): Record {
        val id = map[entity.id.toLong()]
            ?: error("Tried to access components on an entity that no longer exists (${entity.id})")
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
        return mutexes[id] ?: error("Mutex didn't exist for $entity")
    }

    public operator fun contains(entity: GearyEntity): Boolean = map.contains(entity.id.toLong())
}
