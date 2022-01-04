package com.mineinabyss.geary.ecs.accessors

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Accessors allow us to read components off an entity after a data preprocessing step.
 *
 * The whole Accessor system works as follows:
 * - An [AccessorHolder] provides a DSL for creating and registering accessors into it.
 * - A consumer provides an [RawAccessorDataScope] and uses it to create an iterator with [AccessorHolder.iteratorFor].
 * - The [iterator][AccessorHolder.AccessorCombinationsIterator] requests each accessor
 *   to [parse][readData] the raw data.
 * - The consumer creates a [ResultScope], for each iteration.
 * - The [ResultScope]'s scope allows appropriate accessors to read data efficiently.
 */
public abstract class Accessor<T>(
    public val index: Int
): ReadOnlyProperty<GenericResultScope, T> {
    /**
     * An accessor will read data given
     */
    internal abstract fun RawAccessorDataScope.readData(): List<T>

    internal val cached: List<PerArchetypeCache<*>> get() = _cached
    protected val _cached: MutableList<PerArchetypeCache<*>> = mutableListOf()

    protected inline fun <T> cached(crossinline operation: ArchetypeCacheScope.() -> T): PerArchetypeCache<T> =
        object : PerArchetypeCache<T>(_cached.size) {
            override fun ArchetypeCacheScope.calculate(): T = operation()
        }.also { _cached += it }

    public abstract inner class PerArchetypeCache<T>(
        public val cacheIndex: Int
    ) : ReadOnlyProperty<ArchetypeCacheScope, T> {
        public abstract fun ArchetypeCacheScope.calculate(): T

        @Suppress("UNCHECKED_CAST")
        override fun getValue(thisRef: ArchetypeCacheScope, property: KProperty<*>): T =
            thisRef.perArchetypeData[index][cacheIndex] as T
    }

    @Suppress("UNCHECKED_CAST")
    public override fun getValue(thisRef: GenericResultScope, property: KProperty<*>): T =
        thisRef.data[index] as T
}
