package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import java.util.concurrent.ConcurrentHashMap

public class TypeMap {
    private val map: MutableMap<Long, Record> = ConcurrentHashMap<Long, Record>()

    // We don't return nullable record to avoid boxing.
    // Accessing an entity that doesn't exist is indicative of a problem elsewhere and should be made obvious.
    @PublishedApi
    internal fun unsafeGet(entity: GearyEntity): Record = map[entity.id.toLong()]
        ?: error("Tried to access components on an entity that no longer exists (${entity.id})")

    @PublishedApi
    internal fun unsafeSet(entity: GearyEntity, record: Record) {
        if(contains(entity)) error("Tried setting the record of an entity that already exists.")
        map[entity.id.toLong()] = record
    }

    internal fun unsafeRemove(entity: GearyEntity) {
        map.remove(entity.id.toLong())
    }

    public operator fun contains(entity: GearyEntity): Boolean = map.contains(entity.id.toLong())
}
