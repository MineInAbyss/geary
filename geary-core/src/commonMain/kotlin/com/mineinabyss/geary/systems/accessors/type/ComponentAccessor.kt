package com.mineinabyss.geary.systems.accessors.type

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.accessors.AccessorOperations
import com.mineinabyss.geary.systems.accessors.FamilyMatching
import com.mineinabyss.geary.systems.accessors.ReadWriteAccessor
import com.mineinabyss.geary.systems.query.QueriedEntity
import kotlin.reflect.KProperty

@OptIn(UnsafeAccessors::class)
abstract class ComponentAccessor<T>(
    override val queriedEntity: QueriedEntity,
    val id: ComponentId
) : ReadWriteAccessor<T>, FamilyMatching {
    override val family: Family = family { hasSet(id) }

    protected var cachedIndex = -1
    protected var cachedDataArray: MutableList<T> = mutableListOf()
    protected var cachedArchetype: Archetype? = null

    abstract operator fun get(thisRef: AccessorOperations): T

    internal inline fun get(thisRef: AccessorOperations, beforeRead: () -> Unit): T {
        val archetype = queriedEntity.currArchetype
        if (archetype !== cachedArchetype) {
            cachedArchetype = archetype
            cachedIndex = archetype.indexOf(id)
            if (cachedIndex != -1) cachedDataArray = archetype.componentData[cachedIndex] as MutableList<T>
        }
        beforeRead()
        return cachedDataArray[queriedEntity.currRow]
    }

    abstract operator fun set(thisRef: AccessorOperations, value: T)

    internal inline fun set(thisRef: AccessorOperations, value: T, beforeWrite: () -> Unit) {
        val archetype = queriedEntity.currArchetype
        if (archetype !== cachedArchetype) {
            cachedArchetype = archetype
            cachedIndex = archetype.indexOf(id)
            if (cachedIndex != -1) cachedDataArray = archetype.componentData[cachedIndex] as MutableList<T>
        }
        beforeWrite()
        cachedDataArray[queriedEntity.currRow] = value
    }

    final override fun getValue(thisRef: AccessorOperations, property: KProperty<*>): T {
        return get(thisRef)
    }

    final override fun setValue(thisRef: AccessorOperations, property: KProperty<*>, value: T) {
        return set(thisRef, value)
    }
}
