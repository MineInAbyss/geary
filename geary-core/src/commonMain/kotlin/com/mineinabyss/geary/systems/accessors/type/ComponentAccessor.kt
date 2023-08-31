package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.Record
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ComponentAccessor<T>(
    val id: ComponentId,
) : ReadOnlyProperty<Record, T>, FamilyMatching {
    override val family: Family = family { hasSet(id) }

    var cachedIndex = -1
    var cachedArchetype: Archetype? = null

    override fun getValue(thisRef: Record, property: KProperty<*>): T {
        val archetype = thisRef.archetype
        if (archetype != cachedArchetype) {
            cachedArchetype = archetype
            cachedIndex = archetype.indexOf(id)
        }
        return archetype.componentData[cachedIndex][thisRef.row] as T
    }
}
