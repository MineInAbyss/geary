package com.mineinabyss.geary.context

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ContextExtension<T : Any>(
    private val cache: GearyModule.() -> T
) : ReadOnlyProperty<GearyModule, T> {
    private var cachedGlobalContext: GearyModule? = null
    private var cachedValue: T? = null

    override fun getValue(thisRef: GearyModule, property: KProperty<*>): T {
        if (geary != cachedGlobalContext) {
            cachedGlobalContext = geary
            cachedValue = geary.cache()
        }
        return cachedValue!!
    }
}

@Suppress("NOTHING_TO_INLINE") // Lets us use reified type in `cache`
inline fun <T : Any> extend(noinline cache: GearyModule.() -> T): ContextExtension<T> =
    ContextExtension(cache)
