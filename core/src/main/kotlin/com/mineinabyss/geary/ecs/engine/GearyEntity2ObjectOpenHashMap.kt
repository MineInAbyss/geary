package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap


public operator fun <T> Long2ObjectMap<T>.get(k: GearyEntity): T {
    return get(k.id.toLong())
}

public operator fun <T> Long2ObjectMap<T>.set(k: GearyEntity, v: T): T {
    return put(k.id.toLong(), v)
}


public operator fun <T> Long2ObjectMap<T>.get(k: ULong): T {
    return get(k.toLong())
}

public operator fun <T> Long2ObjectMap<T>.set(k: ULong, v: T): T {
    return put(k.toLong(), v)
}
