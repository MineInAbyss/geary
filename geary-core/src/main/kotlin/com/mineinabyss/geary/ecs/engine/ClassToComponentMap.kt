package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyComponentId
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import kotlin.reflect.KClass

@JvmInline
public value class ClassToComponentMap(
    public val map: Object2LongOpenHashMap<KClass<*>> = Object2LongOpenHashMap<KClass<*>>().apply {
        defaultReturnValue(-1)
    }
) {
    public operator fun get(kClass: KClass<*>): GearyComponentId {
        return map.getLong(kClass).toULong()
    }

    public operator fun set(kClass: KClass<*>, id: GearyComponentId) {
        map[kClass] = id.toLong()
    }
}
