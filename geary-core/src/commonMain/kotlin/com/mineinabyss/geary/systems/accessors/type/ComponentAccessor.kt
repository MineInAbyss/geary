package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.Record
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ComponentAccessor<T>(
    val id: ComponentId,
) : ReadOnlyProperty<Record, T>, FamilyMatching {
    override val family: Family = family { hasSet(id) }

    override fun getValue(thisRef: Record, property: KProperty<*>): T {
        val archetype = thisRef.archetype
        return archetype.componentData[archetype.indexOf(id)][thisRef.row] as T
    }
}
