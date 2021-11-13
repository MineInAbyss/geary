package com.mineinabyss.geary.ecs.query.accessors

import com.mineinabyss.geary.ecs.engine.iteration.ArchetypeIterator
import com.mineinabyss.geary.ecs.engine.iteration.accessors.AccessorDataScope
import com.mineinabyss.geary.ecs.engine.iteration.accessors.AccessorHolder
import com.mineinabyss.geary.ecs.engine.iteration.accessors.QueryResult
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Accessors allow us to read components off an entity after a data preprocessing step.
 *
 * The whole Accessor system works as follows:
 * - An [AccessorHolder] provides a DSL for creating and registering accessors into it.
 * - A consumer provides an [AccessorDataScope] and uses it to create an iterator with [AccessorHolder.iteratorFor].
 * - The [iterator][AccessorHolder.AccessorCombinationsIterator] requests each accessor
 *   to [parse][readData] the raw data.
 * - The consumer creates a [QueryResult], for each iteration.
 * - The [QueryResult]'s scope allows appropriate accessors to read data efficiently.
 */
public abstract class Accessor<T>(
    protected val index: Int
) : ReadOnlyProperty<QueryResult, T> {
    override fun getValue(thisRef: QueryResult, property: KProperty<*>): T =
        thisRef.data[index] as T

    /**
     * An accessor will read data given
     */
    internal abstract fun AccessorDataScope.readData(): List<T>

    internal val cached: List<PerIteratorCache<*>> get() = this._cached
    protected val _cached: MutableList<PerIteratorCache<*>> = mutableListOf()

    protected inline fun <T> cached(crossinline operation: ArchetypeIterator.() -> T): PerIteratorCache<T> =
        object : PerIteratorCache<T>(this._cached.size) {
            override fun ArchetypeIterator.calculate(): T = operation()
        }.also { _cached += it }

    public abstract inner class PerIteratorCache<T>(
        private val cacheIndex: Int
    ) : ReadOnlyProperty<ArchetypeIterator, T> {
        public abstract fun ArchetypeIterator.calculate(): T

        override fun getValue(thisRef: ArchetypeIterator, property: KProperty<*>): T =
            thisRef.cachedValues[index][cacheIndex] as T
    }
}
