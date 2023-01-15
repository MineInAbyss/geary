package com.mineinabyss.geary.systems.accessors.types

import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.accessors.*

/**
 * Accessors allow us to read components off an entity after a data preprocessing step.
 *
 * Accessor system summary:
 * - An [AccessorOperationsProvider] provides a DSL for creating accessor builders.
 * - Other classes provide functions for building and adding those accessors onto an [AccessorHolderImpl].
 *   This is done to allow registering multiple holders, ex in [Listener].
 * - A consumer provides an [RawAccessorDataScope] and uses it to create an iterator with [AccessorHolderImpl.iteratorFor].
 * - The [iterator][AccessorHolder.AccessorCombinationsIterator] requests each accessor
 *   to [parse][readData] the raw data.
 * - The consumer creates [ResultScope]s, for each iteration.
 * - The scope allows appropriate accessors to read parsed data without recalculating it each time.
 */
abstract class IndexedAccessor<out T>(
    val index: Int
) : Accessor<T> {
    /**
     * Processes a [RawAccessorDataScope] with an entity.
     *
     * If more than one item is returned, systems will individually handle each combination.
     */
    @PublishedApi
    internal abstract fun RawAccessorDataScope.readData(): List<T>

    /**
     * A list of indices and operations to calculate a cached value on this accessor.
     *
     * @see cached
     */
    internal val cached: List<PerArchetypeCache<*>> get() = _cached
    protected val _cached: MutableList<PerArchetypeCache<*>> = mutableListOf()

    /** Calculates an [operation] once per archetype this accessor gets matched against. */
    protected inline fun <T> cached(crossinline operation: ArchetypeCacheScope.() -> T): PerArchetypeCache<T> =
        object : PerArchetypeCache<T>(index, _cached.size) {
            override fun ArchetypeCacheScope.calculate(): T = operation()
        }.also { _cached += it }

    @Suppress("UNCHECKED_CAST") // Internal logic ensures cast always succeeds
    override fun access(scope: ResultScope): T = scope.data[index] as T
}
