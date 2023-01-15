package com.mineinabyss.geary.systems.accessors.building

import com.mineinabyss.geary.systems.accessors.AccessorBuilder
import com.mineinabyss.geary.systems.accessors.RawAccessorDataScope
import com.mineinabyss.geary.systems.accessors.types.IndexedAccessor

//TODO make test for this accessor
/**
 * Implements the accessor flatten operation given a [wrapped] accessor.
 *
 * @see flatten
 */
open class FlatAccessor<T, A : IndexedAccessor<T>>(
    private val wrapped: A
) : IndexedAccessor<List<T>>(wrapped.index) {
    init {
        _cached.addAll(wrapped.cached)
    }

    override fun RawAccessorDataScope.readData(): List<List<T>> = listOf(wrapped.run { readData() })
}

/**
 * If several combinations are possible (ex several relations present on an entity), will process them as one list
 * instead of handling each individually.
 */
fun <T, A : IndexedAccessor<T>> AccessorBuilder<A>.flatten(): AccessorBuilder<FlatAccessor<T, A>> =
    AccessorBuilder { holder, index ->
        FlatAccessor(this.build(holder, index))
    }
