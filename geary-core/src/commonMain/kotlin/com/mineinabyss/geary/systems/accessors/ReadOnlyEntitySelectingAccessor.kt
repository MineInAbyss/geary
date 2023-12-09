package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.Records
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/** Wrapper for a [ReadOnlyAccessor] that selects a specific entity when multiple can be chosen from. */
open class ReadOnlyEntitySelectingAccessor<T : ReadOnlyAccessor<A>, A>(
    protected val accessor: T,
    protected val pointerIndex: Int,
) : ReadOnlyProperty<Records, A> {
    override fun getValue(thisRef: Records, property: KProperty<*>): A {
        return accessor.getValue(thisRef.getByIndex(pointerIndex), property)
    }
}
