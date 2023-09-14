package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import com.mineinabyss.geary.systems.accessors.Pointer
import com.mineinabyss.geary.systems.accessors.ReadWriteAccessor
import kotlin.reflect.KProperty

@OptIn(UnsafeAccessors::class)
abstract class ComponentAccessor<T>(
    val id: ComponentId
) : ReadWriteAccessor<T>, FamilyMatching {
    override val family: Family = family { hasSet(id) }

    protected var cachedIndex = -1
    protected var cachedDataArray: MutableList<T> = mutableListOf()
    protected var cachedArchetype: Archetype? = null

    abstract operator fun get(thisRef: Pointer): T

    internal inline fun get(thisRef: Pointer, beforeRead: () -> Unit): T {
        val archetype = thisRef.archetype
        if (archetype !== cachedArchetype) {
            cachedArchetype = archetype
            cachedIndex = archetype.indexOf(id)
            if (cachedIndex != -1) cachedDataArray = archetype.componentData[cachedIndex] as MutableList<T>
        }
        beforeRead()
        return cachedDataArray[thisRef.row]
    }

    abstract operator fun set(thisRef: Pointer, value: T)

    internal inline fun set(thisRef: Pointer, value: T, beforeWrite: () -> Unit) {
        val archetype = thisRef.archetype
        if (archetype !== cachedArchetype) {
            cachedArchetype = archetype
            cachedIndex = archetype.indexOf(id)
            if (cachedIndex != -1) cachedDataArray = archetype.componentData[cachedIndex] as MutableList<T>
        }
        beforeWrite()
        cachedDataArray[thisRef.row] = value
    }

    final override fun getValue(thisRef: Pointer, property: KProperty<*>): T {
        return get(thisRef)
    }

    final override fun setValue(thisRef: Pointer, property: KProperty<*>, value: T) {
        return set(thisRef, value)
    }
}
