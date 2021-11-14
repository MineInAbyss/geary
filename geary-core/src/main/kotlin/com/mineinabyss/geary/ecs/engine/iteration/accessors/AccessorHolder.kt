package com.mineinabyss.geary.ecs.engine.iteration.accessors

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.relations.RelationParent
import com.mineinabyss.geary.ecs.api.systems.MutableAndSelector
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import com.mineinabyss.geary.ecs.query.AndSelector
import com.mineinabyss.geary.ecs.query.accessors.*

/**
 * A holder of [Accessor]s which provides helper functions for creating them.
 *
 * @property family A lazily built immutable family that represents all data this holder needs to function.
 */
public abstract class AccessorHolder : MutableAndSelector() {
    public val family: AndSelector by lazy { build() }
    internal val accessors = mutableListOf<Accessor<*>>()
    private val perArchetypeCache = mutableMapOf<Archetype, List<List<Any?>>>()

    //TODO getOrNull

    protected inline fun <reified T : GearyComponent> get(): ComponentAccessor<T> {
        val component = componentId<T>() or HOLDS_DATA
        has(component)
        return addAccessor { ComponentAccessor(it, component) }
    }

    public inline fun <reified T : GearyComponent> relation(): RelationAccessor<T> {
        val relationParent = RelationParent(componentId<T>())
        has(relationParent)
        return addAccessor { RelationAccessor(it, relationParent) }
    }

    public inline fun <reified T : GearyComponent> relationWithData(): RelationWithDataAccessor<T> {
        val relationParent = RelationParent(componentId<T>())
        has(relationParent, componentMustHoldData = true)

        return addAccessor { RelationWithDataAccessor(it, relationParent) }
    }

    public inline fun <reified T : GearyComponent> allRelationsWithData(): RelationListAccessor<T> {
        val relationParent = RelationParent(componentId<T>())
        has(relationParent, componentMustHoldData = true)
        return addAccessor { RelationListAccessor(it, relationParent) }
    }

    public fun <T : Accessor<*>> addAccessor(create: (index: Int) -> T): T {
        val accessor = create(accessors.size)
        accessors += accessor
        return accessor
    }

    public fun cacheForArchetype(archetype: Archetype): List<List<Any?>> =
        perArchetypeCache.getOrPut(archetype) {
            val accessorCache: List<MutableList<Any?>> = accessors.map { it.cached.mapTo(mutableListOf()) { null } }
            val cache = ArchetypeCache(archetype, accessorCache)

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
