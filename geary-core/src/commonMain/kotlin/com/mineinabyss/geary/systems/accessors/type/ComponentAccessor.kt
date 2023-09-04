package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.UnsafeAccessors
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.accessors.AccessorThisRef
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import com.mineinabyss.geary.systems.accessors.ReadWriteAccessor
import kotlin.reflect.KProperty

@OptIn(UnsafeAccessors::class)
open class ComponentAccessor<T : Any>(
    val id: ComponentId,
): ReadWriteAccessor<T>, FamilyMatching {
    override val family: Family = family { hasSet(id) }

    private var cachedIndex = -1
    private var cachedDataArray: MutableList<T> = mutableListOf()
    private var cachedArchetype: Archetype? = null

    operator fun get(thisRef: AccessorThisRef): T {
        val archetype = thisRef.archetype
        if (archetype !== cachedArchetype) {
            cachedArchetype = archetype
            cachedIndex = archetype.indexOf(id)
            cachedDataArray = archetype.componentData[cachedIndex] as MutableList<T>
        }
        return cachedDataArray[thisRef.row]
    }

    operator fun set(thisRef: AccessorThisRef, value: T) {
        val archetype = thisRef.archetype
        if (archetype !== cachedArchetype) {
            cachedArchetype = archetype
            cachedIndex = archetype.indexOf(id)
            if (cachedIndex == -1) {
                thisRef.entity.set(value, id)
                return
            }
            cachedDataArray = archetype.componentData[cachedIndex] as MutableList<T>
        }
        cachedDataArray[thisRef.row] = value
    }

    override fun getValue(thisRef: AccessorThisRef, property: KProperty<*>): T {
        return get(thisRef)
    }

    override fun setValue(thisRef: AccessorThisRef, property: KProperty<*>, value: T) {
        return set(thisRef, value)
    }
}
