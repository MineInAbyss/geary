package com.mineinabyss.geary.systems.accessors.type

import androidx.collection.MutableObjectList
import androidx.collection.mutableObjectListOf
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
    private var cachedDataArray: MutableObjectList<T> = mutableObjectListOf()

    fun updateCache(archetype: Archetype) {
        cachedIndex = archetype.indexOf(id)
        @Suppress("UNCHECKED_CAST")
        if (cachedIndex != -1) cachedDataArray = archetype.componentData[cachedIndex] as MutableObjectList<T>
    }

    override fun get(query: Query): T {
        return cachedDataArray[query.row]
    }

    override fun set(query: Query, value: T) {
        cachedDataArray[query.row] = value
    }
}
