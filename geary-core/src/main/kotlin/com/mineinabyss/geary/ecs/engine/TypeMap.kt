package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyEntityId
import it.unimi.dsi.fastutil.longs.Long2LongMap
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap

@JvmInline
public value class TypeMap(public val map: Long2LongMap = Long2LongOpenHashMap()) {
    public operator fun contains(id: GearyEntityId): Boolean = map.contains(id.toLong())

    // We don't return nullable record to avoid boxing.
    // Since this is internal, we just need to be careful to always do a contains check first.
    public operator fun get(id: GearyEntityId): Record = Record(map[id.toLong()])

    public operator fun set(id: GearyEntityId, record: Record) {
        map[id.toLong()] = record.id
    }

    public fun remove(id: GearyEntityId) {
        map.remove(id.toLong())
    }
}
