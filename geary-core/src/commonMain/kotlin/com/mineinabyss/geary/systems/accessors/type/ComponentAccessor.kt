package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.accessors.Accessor
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import com.mineinabyss.geary.systems.accessors.ReadWriteAccessor
import com.mineinabyss.geary.systems.query.Query
import kotlin.reflect.KProperty

@OptIn(UnsafeAccessors::class)
class ComponentAccessor<T : Any>(
    override val originalAccessor: Accessor?,
    val id: ComponentId
) : ReadWriteAccessor<T>, FamilyMatching {
    override val family = family { hasSet(id) }

    private var cachedIndex = -1
    private var cachedDataArray: MutableList<T> = mutableListOf()

    fun updateCache(archetype: Archetype) {
        cachedIndex = archetype.indexOf(id)
        @Suppress("UNCHECKED_CAST")
        if (cachedIndex != -1) cachedDataArray = archetype.componentData[cachedIndex] as MutableList<T>
    }

    fun get(query: Query): T {
        return cachedDataArray[query.row]
    }

    fun set(query: Query, value: T) {
        cachedDataArray[query.row] = value
    }

    override fun getValue(thisRef: Query, property: KProperty<*>): T {
        return get(thisRef)
    }

    override fun setValue(thisRef: Query, property: KProperty<*>, value: T) {
        return set(thisRef, value)
    }
}
