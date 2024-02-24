package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.systems.accessors.AccessorOperations
import com.mineinabyss.geary.systems.accessors.ReadOnlyAccessor
import kotlin.reflect.KProperty

class MappedAccessor<T, U>(val original: ReadOnlyAccessor<T>, val mapping: (T) -> U) : ReadOnlyAccessor<U> {
    override fun getValue(thisRef: AccessorOperations, property: KProperty<*>): U {
        val value = original.getValue(thisRef, property)
        return mapping(value)
    }
}
