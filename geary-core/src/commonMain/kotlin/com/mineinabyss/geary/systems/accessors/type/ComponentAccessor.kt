package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.accessors.Accessor
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import com.mineinabyss.geary.systems.accessors.ReadWriteAccessor
import com.mineinabyss.geary.systems.query.QueriedEntity
import com.mineinabyss.geary.systems.query.Query
import kotlin.reflect.KProperty

@OptIn(UnsafeAccessors::class)
abstract class ComponentAccessor<T>(
    val cacheArchetypeInfo: Boolean,
    override val originalAccessor: Accessor?,
    val id: ComponentId
) : ReadWriteAccessor<T>, FamilyMatching {
    override val family = family { hasSet(id) }

    protected var cachedIndex = -1
    protected var cachedDataArray: MutableList<T> = mutableListOf()

    fun updateCache(archetype: Archetype) {
        cachedIndex = archetype.indexOf(id)
        @Suppress("UNCHECKED_CAST")
        if (cachedIndex != -1) cachedDataArray = archetype.componentData[cachedIndex] as MutableList<T>
    }

    fun get(query: Query): T {
//        if (!cacheArchetypeInfo) {
//            updateCache(query.archetype)
//            return cachedDataArray[query.row]
//        }
        return cachedDataArray[query.row]
    }

    fun set(query: Query, value: T) {
//        if (!cacheArchetypeInfo) {
//            updateCache(query.archetype)
//            if (value == null) {
//                query.unsafeEntity.remove(id)
//                return
//            }
//            if (cachedIndex == -1) query.unsafeEntity.set(value, id)
//            cachedDataArray[query.row] = value
//            return
//        }
        if(value == null) TODO("Nullable case not implemented")
        cachedDataArray[query.row] = value
    }

    final override fun getValue(thisRef: Query, property: KProperty<*>): T {
        return get(thisRef)
    }

    final override fun setValue(thisRef: Query, property: KProperty<*>, value: T) {
        return set(thisRef, value)
    }
}
