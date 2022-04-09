package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyComponentId
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

@JvmInline
public value class ClassToComponentMap(
    public val map: MutableMap<KClass<*>, Long> = mutableMapOf()
) {
    public operator fun get(kClass: KClass<*>): GearyComponentId {
        return (map[kClass] ?: -1).toULong()
    }

    public operator fun set(kClass: KClass<*>, id: GearyComponentId) {
        map[kClass] = id.toLong()
    }
}
