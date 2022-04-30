package com.mineinabyss.geary.context

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

public class ContextExtension<T : Any>(
    private val cache: GearyContext.() -> T
) : ReadOnlyProperty<GearyContext, T> {
    private var cachedGlobalContext: GearyContext? = null
    private var cachedValue: T? = null

    override fun getValue(thisRef: GearyContext, property: KProperty<*>): T {
        if (globalContext != cachedGlobalContext) {
            cachedGlobalContext = globalContext
            cachedValue = globalContext.cache()
        }
        return cachedValue!!
    }
}

@Suppress("NOTHING_TO_INLINE") // Lets us use reified type in `cache`
public inline fun <T : Any> extend(noinline cache: GearyContext.() -> T): ContextExtension<T> =
    ContextExtension(cache)
