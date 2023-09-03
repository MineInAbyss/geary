package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.Records
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/** Wrapper for a [ReadWriteAccessor] that selects a specific entity when multiple can be chosen from. */
class ReadWriteEntitySelectingAccessor<T : ReadWriteAccessor<A>, A>(
    accessor: T,
    pointerIndex: Int
) : ReadOnlyEntitySelectingAccessor<T, A>(accessor, pointerIndex), ReadWriteProperty<Records, A> {
    override fun setValue(thisRef: Records, property: KProperty<*>, value: A) {
        accessor.setValue(thisRef.getByIndex(pointerIndex), property, value)
    }
}
