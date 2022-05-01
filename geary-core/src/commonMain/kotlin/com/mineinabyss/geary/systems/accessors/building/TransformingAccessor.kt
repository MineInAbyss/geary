package com.mineinabyss.geary.systems.accessors.building

import com.mineinabyss.geary.systems.accessors.AccessorBuilder
import com.mineinabyss.geary.systems.accessors.types.IndexedAccessor
import com.mineinabyss.geary.systems.accessors.RawAccessorDataScope

/**
 * Implements the accessor [map] operation given a [wrapped] accessor.
 *
 * @see map
 */
public class TransformingAccessor<T, R>(
    private val transform: (T) -> R,
    private val wrapped: IndexedAccessor<T>
) : IndexedAccessor<R>(wrapped.index) {
    init {
        _cached.addAll(wrapped.cached)
    }

    override fun RawAccessorDataScope.readData(): List<R> {
        return wrapped.run { readData() }.map(transform)
    }
}

/** Takes the result of another accessor and [transform]s it. */
public fun <T, R, A : IndexedAccessor<T>> AccessorBuilder<A>.map(
    transform: (T) -> R
): AccessorBuilder<IndexedAccessor<R>> = AccessorBuilder { holder, index ->
    TransformingAccessor(transform, this.build(holder, index))
}
