package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.Record
import com.mineinabyss.geary.datatypes.Records
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class EntitySelectingAccessor<T : ReadOnlyProperty<Record, A>, A>(
    private val accessor: T,
    private val pointerIndex: Int,
) : ReadOnlyProperty<Records, A> {
    override fun getValue(thisRef: Records, property: KProperty<*>): A {
        return accessor.getValue(thisRef.getByIndex(pointerIndex), property)
    }
}
