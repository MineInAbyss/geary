package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.systems.accessors.Accessor
import com.mineinabyss.geary.systems.accessors.type.ComponentAccessor
import kotlin.reflect.KProperty

abstract class Query : QueriedEntity(cacheAccessors = true) {
    /** Automatically matches families for any accessor that's supposed to match a family. */
    operator fun <T : Accessor> T.provideDelegate(
        thisRef: Any,
        prop: KProperty<*>
    ): T {
        queriedEntity.props[prop.name] = this
        return this
    }

    protected open fun ensure() {}

    internal fun initialize() {
        ensure()
    }

    // Optional helpers for avoiding delegates in accessors

    inline operator fun <T> ComponentAccessor<T>.invoke(): T = get(this@Query)
    inline fun <T> ComponentAccessor<T>.set(value: T) = set(this@Query, value)
}
