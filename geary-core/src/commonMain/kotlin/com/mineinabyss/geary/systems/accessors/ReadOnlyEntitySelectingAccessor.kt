package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.Records
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class ReadOnlyEntitySelectingAccessor<T : ReadOnlyAccessor<A>, A>(
    protected val accessor: T,
    protected val pointerIndex: Int,
) : ReadOnlyProperty<Records, A> {
    override fun getValue(thisRef: Records, property: KProperty<*>): A {
        return accessor.getValue(thisRef.getByIndex(pointerIndex), property)
    }
}
