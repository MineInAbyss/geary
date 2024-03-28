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
    final override val queriedEntity: QueriedEntity,
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

    abstract fun get(thisRef: Query): T

    internal inline fun get(query: Query, beforeRead: () -> Unit): T {
        beforeRead()
        if (!cacheArchetypeInfo) {
            updateCache(queriedEntity.archetype)
            return cachedDataArray[queriedEntity.row]
        }
        return cachedDataArray[query.row]
    }

    abstract fun set(query: Query, value: T)

    internal inline fun set(query: Query, value: T, beforeWrite: () -> Unit) {
        beforeWrite()
        if (!cacheArchetypeInfo) {
            updateCache(query.archetype)
            if (value == null) {
                queriedEntity.unsafeEntity.remove(id)
                return
            }
            if (cachedIndex == -1) queriedEntity.unsafeEntity.set(value, id)
            else cachedDataArray[queriedEntity.row] = value
            return
        }
        cachedDataArray[query.row] = value
    }

    final override fun getValue(thisRef: Query, property: KProperty<*>): T {
        return get(thisRef)
    }

    final override fun setValue(thisRef: Query, property: KProperty<*>, value: T) {
        return set(thisRef, value)
    }
}
