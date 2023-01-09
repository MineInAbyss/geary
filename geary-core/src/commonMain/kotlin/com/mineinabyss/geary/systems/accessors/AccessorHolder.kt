package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.MutableFamily
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.systems.accessors.types.DirectAccessor
import com.mineinabyss.geary.systems.accessors.types.IndexedAccessor
import com.soywiz.kds.FastIntMap
import com.soywiz.kds.getOrPut
import kotlin.reflect.KProperty


/**
 * A holder of [IndexedAccessor]s that provides logic for reading data off them and calculating their per-archetype cache.
 *
 * @property family A lazily built immutable family that represents all data this holder needs to function.
 */
open class AccessorHolder: AccessorOperations() {
    val family: Family.Selector.And get() = _family

    @PublishedApi
    internal val _family: MutableFamily.Selector.And = MutableFamily.Selector.And()

    @PublishedApi
    internal open val accessors: MutableList<IndexedAccessor<*>> = mutableListOf()
    private val perArchetypeCache = FastIntMap<List<List<Any?>>>()

    operator fun <T : IndexedAccessor<*>> AccessorBuilder<T>.provideDelegate(
        thisRef: Any,
        property: KProperty<*>
    ): T = addAccessor { build(this@AccessorHolder, it) }

    inline fun <T : Accessor<*>> addAccessor(create: (index: Int) -> T): T {
        val accessor = create(accessors.size)
        when (accessor) {
            is IndexedAccessor<*> -> accessors += accessor
            is DirectAccessor<*> -> {}
            else -> error("Accessor from unknown backend, ignoring.")
        }
        return accessor
    }

    /** Calculates, or gets cached values for an [archetype] */
    fun cacheForArchetype(archetype: Archetype): List<List<Any?>> =
        perArchetypeCache.getOrPut(archetype.id) {
            val accessorCache: List<MutableList<Any?>> = accessors.map { it.cached.mapTo(mutableListOf()) { null } }
            val cache = ArchetypeCacheScope(archetype, accessorCache)

            for (accessor in accessors)
                for (it in accessor.cached)
                    accessorCache[accessor.index][it.cacheIndex] =
                        it.run { cache.calculate() }

            accessorCache
        }

    /** Iterates over data in [dataScope] with all possible combinations calculated by accessors in this holder. */
    @PublishedApi
    internal inline fun forEachCombination(dataScope: RawAccessorDataScope, run: (List<*>) -> Unit) {
        // All sets of data each accessor wants. Will iterate over all combinations of items from each list.
        val data: List<List<*>> = accessors.map { with(it) { dataScope.readData() } }
        // The total number of combinations that can be made with all elements in each list.
        val totalCombinations = data.fold(1) { acc, b -> acc * b.size }
        for (permutation in 0 until totalCombinations) {
            run(data.map { it[permutation % it.size] })
        }
    }

    /** Is the family of this holder not restricted in any way? */
    val isEmpty: Boolean get() = family.and.isEmpty()
}
