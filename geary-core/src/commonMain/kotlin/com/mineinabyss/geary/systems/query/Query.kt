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

    @PublishedApi
    internal fun initialize() {
        ensure()
    }

    // Optional helpers for avoiding delegates in accessors

    @Suppress("NOTHING_TO_INLINE") // These functions are here for maximum speed over delegates, we can inline :)
    inline operator fun <T> ComponentAccessor<T>.invoke(): T = get(this@Query)

    @Suppress("NOTHING_TO_INLINE")
    inline fun <T> ComponentAccessor<T>.set(value: T) = set(this@Query, value)
}
