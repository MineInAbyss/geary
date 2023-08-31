package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.Record
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ComponentOrDefaultAccessor<T>(
    val id: ComponentId,
    val default: () -> T,
) : ReadOnlyProperty<Record, T> {
    override fun getValue(thisRef: Record, property: KProperty<*>): T {
        val archetype = thisRef.archetype
        val index = archetype.indexOf(id)
        if (index == -1) return default()
        return archetype.componentData[index][thisRef.row] as T
    }
}
