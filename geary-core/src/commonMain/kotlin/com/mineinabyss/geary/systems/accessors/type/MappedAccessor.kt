package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.systems.accessors.AccessorOperations
import com.mineinabyss.geary.systems.accessors.ReadOnlyAccessor
import com.mineinabyss.geary.systems.query.QueriedEntity
import com.mineinabyss.geary.systems.query.Query
import kotlin.reflect.KProperty

class MappedAccessor<T, U>(
    override val originalAccessor: ReadOnlyAccessor<T>,
    val mapping: (T) -> U,
) : ReadOnlyAccessor<U> {
    override val queriedEntity: QueriedEntity = originalAccessor.queriedEntity
    override fun getValue(thisRef: Query, property: KProperty<*>): U {
        val value = originalAccessor.getValue(thisRef, property)
        return mapping(value)
    }
}
