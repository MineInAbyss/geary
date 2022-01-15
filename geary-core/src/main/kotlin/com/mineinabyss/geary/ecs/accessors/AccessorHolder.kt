package com.mineinabyss.geary.ecs.accessors

import com.mineinabyss.geary.ecs.accessors.building.AccessorBuilder
import com.mineinabyss.geary.ecs.accessors.building.AccessorBuilderProvider
import com.mineinabyss.geary.ecs.api.systems.MutableAndSelector
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.query.AndSelector
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import kotlin.reflect.KProperty


/**
 * A holder of [Accessor]s that provides logic for reading data off them and calculating their per-archetype cache.
 *
 * @property family A lazily built immutable family that represents all data this holder needs to function.
 */
public open class AccessorHolder : MutableAndSelector(), AccessorBuilderProvider {
    public val family: AndSelector by lazy { build() }
    internal open val accessors = mutableListOf<Accessor<*>>()
    private val perArchetypeCache = Int2ObjectOpenHashMap<List<List<Any?>>>()

    public operator fun <T : Accessor<*>> AccessorBuilder<T>.provideDelegate(
        thisRef: Any,
        property: KProperty<*>
    ): T = addAccessor { build(this@AccessorHolder, it) }

    public open fun <T : Accessor<*>> addAccessor(create: (index: Int) -> T): T {
        val accessor = create(accessors.size)
        accessors += accessor
        return accessor
    }

    /** Calculates, or gets cached values for an [archetype] */
    //TODO return inline class for type safety
    public fun cacheForArchetype(archetype: Archetype): List<List<Any?>> =
        perArchetypeCache.getOrPut(archetype.id) {
            val accessorCache: List<MutableList<Any?>> = accessors.map { it.cached.mapTo(mutableListOf()) { null } }
            val cache = ArchetypeCacheScope(archetype, accessorCache)

            for (accessor in accessors)
                for (it in accessor.cached)
                    accessorCache[accessor.index][it.cacheIndex] =
                        it.run { cache.calculate() }

            accessorCache
        }

    /** Gets an iterator that will process [dataScope] with all possible combinations calculated by Accessors */
    internal fun iteratorFor(dataScope: RawAccessorDataScope): AccessorCombinationsIterator =
        AccessorCombinationsIterator(dataScope)

    internal inner class AccessorCombinationsIterator(val dataScope: RawAccessorDataScope) : Iterator<List<*>> {
        /** All sets of data each accessor wants. Will iterate over all combinations of items from each list. */
        private val data: List<List<*>> = accessors.map { with(it) { dataScope.readData() } }

        /** The total number of combinations that can be made with all elements in each list. */
        private val totalCombinations = data.fold(1) { acc, b -> acc * b.size }
        private var index = 0

        override fun hasNext() = index < totalCombinations

        override fun next(): List<*> {
            val permutation = index++
            return data.map { it[permutation % it.size] }
        }
    }

    /** Is the family of this holder not restricted in any way? */
    public val isEmpty: Boolean get() = family.and.isEmpty()
}
