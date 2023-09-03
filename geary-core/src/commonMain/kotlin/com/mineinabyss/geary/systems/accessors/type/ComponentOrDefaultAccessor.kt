package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.UnsafeAccessors
import com.mineinabyss.geary.systems.accessors.AccessorThisRef
import com.mineinabyss.geary.systems.accessors.ReadOnlyAccessor
import kotlin.reflect.KProperty

@OptIn(UnsafeAccessors::class)
class ComponentOrDefaultAccessor<T>(
    val id: ComponentId,
    val default: () -> T,
) : ReadOnlyAccessor<T> {
    override fun getValue(thisRef: AccessorThisRef, property: KProperty<*>): T {
        val archetype = thisRef.archetype
        val index = archetype.indexOf(id)
        if (index == -1) return default()
        return archetype.componentData[index][thisRef.row] as T
    }
}
