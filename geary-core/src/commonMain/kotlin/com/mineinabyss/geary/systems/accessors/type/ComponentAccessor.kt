package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import com.mineinabyss.geary.systems.accessors.Pointer
import com.mineinabyss.geary.systems.accessors.ReadWriteAccessor
import com.mineinabyss.geary.systems.query.QueriedEntity
import com.mineinabyss.geary.systems.query.Query
import kotlin.reflect.KProperty

@OptIn(UnsafeAccessors::class)
abstract class ComponentAccessor<T>(
    val entity: QueriedEntity,
    val id: ComponentId
) : ReadWriteAccessor<T>, FamilyMatching {
    override val family: Family = family { hasSet(id) }

    protected var cachedIndex = -1
    protected var cachedDataArray: MutableList<T> = mutableListOf()
    protected var cachedArchetype: Archetype? = null

    abstract operator fun get(thisRef: Query): T

    internal inline fun get(thisRef: Query, beforeRead: () -> Unit): T {
        val archetype = entity.currArchetype
        if (archetype !== cachedArchetype) {
            cachedArchetype = archetype
            cachedIndex = archetype.indexOf(id)
            if (cachedIndex != -1) cachedDataArray = archetype.componentData[cachedIndex] as MutableList<T>
        }
        beforeRead()
        return cachedDataArray[entity.currEntityIndex]
    }

    abstract operator fun set(thisRef: Query, value: T)

    internal inline fun set(thisRef: Query, value: T, beforeWrite: () -> Unit) {
        val archetype = entity.currArchetype
        if (archetype !== cachedArchetype) {
            cachedArchetype = archetype
            cachedIndex = archetype.indexOf(id)
            if (cachedIndex != -1) cachedDataArray = archetype.componentData[cachedIndex] as MutableList<T>
        }
        beforeWrite()
        cachedDataArray[entity.currEntityIndex] = value
    }

    final override fun getValue(thisRef: Query, property: KProperty<*>): T {
        return get(thisRef)
    }

    final override fun setValue(thisRef: Query, property: KProperty<*>, value: T) {
        return set(thisRef, value)
    }
}
