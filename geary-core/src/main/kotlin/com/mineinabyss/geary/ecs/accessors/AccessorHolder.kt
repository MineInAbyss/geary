package com.mineinabyss.geary.ecs.accessors

import com.mineinabyss.geary.ecs.api.systems.MutableAndSelector
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.query.AndSelector
import kotlin.reflect.KProperty


/**
 * A holder of [Accessor]s which provides helper functions for creating them.
 *
 * @property family A lazily built immutable family that represents all data this holder needs to function.
 */
public abstract class AccessorHolder : MutableAndSelector(), AccessorBuilderProvider {
    public val family: AndSelector by lazy { build() }
    internal open val accessors = mutableListOf<Accessor<*>>()
    private val perArchetypeCache = mutableMapOf<Archetype, List<List<Any?>>>()

    public operator fun <T : Accessor<*>> AccessorBuilder<T>.provideDelegate(
        thisRef: Any,
        property: KProperty<*>
    ): T = addAccessor { build(this@AccessorHolder, it) }

    public open fun <T : Accessor<*>> addAccessor(create: (index: Int) -> T): T {
        val accessor = create(accessors.size)
        accessors += accessor
        return accessor
    }

    public fun cacheForArchetype(archetype: Archetype): List<List<Any?>> =
        perArchetypeCache.getOrPut(archetype) {
            val accessorCache: List<MutableList<Any?>> = accessors.map { it.cached.mapTo(mutableListOf()) { null } }
            val cache = ArchetypeCacheScope(archetype, accessorCache)

            for (accessor in accessors)
                for (it in accessor.cached)
                    accessorCache[accessor.index][it.cacheIndex] =
                        it.run { cache.calculate() }

            accessorCache
        }

    // ==== Iteration ====

    internal fun iteratorFor(dataScope: RawAccessorDataScope): AccessorCombinationsIterator =
        AccessorCombinationsIterator(dataScope)

    internal inner class AccessorCombinationsIterator(val dataScope: RawAccessorDataScope) : Iterator<List<*>> {
        /** All sets of data each accessor wants. Will iterate over all combinations of items from each list. */
        val data: List<List<*>> = accessors.map { with(it) { dataScope.readData() } }

        /** The total number of combinations that can be made with all elements in each list. */
        val combinationsCount = data.fold(1) { acc, b -> acc * b.size }
        var permutation = 0

        override fun hasNext() = permutation < combinationsCount

        override fun next(): List<*> {
            val permutation = permutation++
            return data.map { it[permutation % it.size] }
        }
    }
}
