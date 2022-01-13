package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import it.unimi.dsi.fastutil.longs.Long2LongMap
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap

@JvmInline
public value class TypeMap(public val map: Long2LongMap = Long2LongOpenHashMap()) {
    public operator fun contains(entity: GearyEntity): Boolean = map.contains(entity.id.toLong())

    // We don't return nullable record to avoid boxing.
    // Accessing an entity that doesn't exist is indicative of a problem elsewhere and should be made obvious.
    public operator fun get(entity: GearyEntity): Record {
        val id = map[entity.id.toLong()]
        if (id == -1L) error("Tried to access components on an entity that no longer exists (${entity.id})")
        return Record(id)
    }

    public operator fun set(entity: GearyEntity, record: Record) {
        map[entity.id.toLong()] = record.id
    }

    public fun remove(entity: GearyEntity) {
        map.remove(entity.id.toLong())
    }
}
