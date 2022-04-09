package com.mineinabyss.geary.ecs.accessors.building

import com.mineinabyss.geary.ecs.accessors.Accessor
import com.mineinabyss.geary.ecs.accessors.RawAccessorDataScope

/**
 * Implements the accessor [map] operation given a [wrapped] accessor.
 *
 * @see map
 */
public class TransformingAccessor<T, R>(
    private val transform: (T) -> R,
    private val wrapped: Accessor<T>
) : Accessor<R>(wrapped.index) {
    init {
        _cached.addAll(wrapped.cached)
    }

    override fun RawAccessorDataScope.readData(): List<R> {
        return wrapped.run { readData() }.map(transform)
    }
}

/** Takes the result of another accessor and [transform]s it. */
public fun <T, R, A : Accessor<T>> AccessorBuilder<A>.map(
    transform: (T) -> R
): AccessorBuilder<Accessor<R>> = AccessorBuilder { holder, index ->
    TransformingAccessor(transform, this.build(holder, index))
}
