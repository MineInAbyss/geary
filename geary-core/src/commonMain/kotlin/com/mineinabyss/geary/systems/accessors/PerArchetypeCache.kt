package com.mineinabyss.geary.systems.accessors

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Handles retrieving and calculating values per archetype with an assigned index for an [Accessor].
 *
 * @property index The accessor's index in its holder.
 * @property cacheIndex An assigned index to store/read data from.
 */
abstract class PerArchetypeCache<T>(
    val index: Int,
    val cacheIndex: Int
) : ReadOnlyProperty<ArchetypeCacheScope, T> {
    abstract fun ArchetypeCacheScope.calculate(): T

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: ArchetypeCacheScope, property: KProperty<*>): T =
        thisRef.perArchetypeData[index][cacheIndex] as T
}
