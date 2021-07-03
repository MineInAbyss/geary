package com.mineinabyss.geary.ecs.engine.iteration

import com.mineinabyss.geary.ecs.query.Query
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

public abstract class Accessor<T>(
    private val query: Query
) : ReadOnlyProperty<QueryResult, T> {
    init {
        query.accessors += this
    }

    private val index = query.accessors.lastIndex

    override fun getValue(thisRef: QueryResult, property: KProperty<*>): T =
        thisRef.data[index] as T

    internal abstract fun AccessorData.readData(): List<T>

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
