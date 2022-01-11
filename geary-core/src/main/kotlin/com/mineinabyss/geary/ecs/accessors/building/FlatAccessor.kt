package com.mineinabyss.geary.ecs.accessors.building

import com.mineinabyss.geary.ecs.accessors.Accessor
import com.mineinabyss.geary.ecs.accessors.RawAccessorDataScope

//TODO make test for this accessor
/**
 * Implements the accessor flatten operation given a [wrapped] accessor.
 *
 * @see flatten
 */
public open class FlatAccessor<T, A : Accessor<T>>(
    private val wrapped: A
) : Accessor<List<T>>(wrapped.index) {
    init {
        _cached.addAll(wrapped.cached)
    }

    override fun RawAccessorDataScope.readData(): List<List<T>> = listOf(wrapped.run { readData() })
}

/**
 * If several combinations are possible (ex several relations present on an entity), will process them as one list
 * instead of handling each individually.
 */
public fun <T, A : Accessor<T>> AccessorBuilder<A>.flatten(): AccessorBuilder<FlatAccessor<T, A>> =
    AccessorBuilder { holder, index ->
        FlatAccessor(this.build(holder, index))
    }
