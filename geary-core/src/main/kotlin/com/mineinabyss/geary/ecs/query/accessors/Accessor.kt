package com.mineinabyss.geary.ecs.query.accessors

import com.mineinabyss.geary.ecs.engine.iteration.accessors.AccessorHolder
import com.mineinabyss.geary.ecs.engine.iteration.accessors.ArchetypeCache
import com.mineinabyss.geary.ecs.engine.iteration.accessors.QueryResult
import com.mineinabyss.geary.ecs.engine.iteration.accessors.RawAccessorDataScope
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
 * - The consumer creates a [QueryResult], for each iteration.
 * - The [QueryResult]'s scope allows appropriate accessors to read data efficiently.
 */
public abstract class Accessor<T>(
    public val index: Int
) : ReadOnlyProperty<QueryResult, T> {
    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: QueryResult, property: KProperty<*>): T =
        thisRef.data[index] as T

    /**
     * An accessor will read data given
     */
    internal abstract fun RawAccessorDataScope.readData(): List<T>

    internal val cached: List<PerArchetypeCache<*>> get() = _cached
    protected val _cached: MutableList<PerArchetypeCache<*>> = mutableListOf()

    protected inline fun <T> cached(crossinline operation: ArchetypeCache.() -> T): PerArchetypeCache<T> =
        object : PerArchetypeCache<T>(_cached.size) {
            override fun ArchetypeCache.calculate(): T = operation()
        }.also { _cached += it }

    public abstract inner class PerArchetypeCache<T>(
        public val cacheIndex: Int
    ) : ReadOnlyProperty<ArchetypeCache, T> {
        public abstract fun ArchetypeCache.calculate(): T

        @Suppress("UNCHECKED_CAST")
        override fun getValue(thisRef: ArchetypeCache, property: KProperty<*>): T =
            thisRef.perArchetypeData[index][cacheIndex] as T
    }
}
