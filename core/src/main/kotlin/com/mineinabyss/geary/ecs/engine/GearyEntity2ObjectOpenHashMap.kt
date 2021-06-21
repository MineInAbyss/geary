package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap


public fun <T> Long2ObjectMap<T>.get(k: GearyEntity): T {
    return get(k.id.toLong())
}

public fun <T> Long2ObjectMap<T>.put(k: GearyEntity, v: T): T {
    return put(k.id.toLong(), v)
}
