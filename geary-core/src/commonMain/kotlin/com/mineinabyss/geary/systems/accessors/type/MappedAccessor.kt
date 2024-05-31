package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.systems.accessors.ReadOnlyAccessor
import com.mineinabyss.geary.systems.query.Query

class MappedAccessor<T, U>(
    override val originalAccessor: ReadOnlyAccessor<T>,
    val mapping: (T) -> U,
) : ReadOnlyAccessor<U> {
    override fun get(query: Query): U {
        val value = originalAccessor.get(query)
        return mapping(value)
    }
}
