package com.mineinabyss.geary.ecs.accessors

import com.mineinabyss.geary.ecs.accessors.building.AccessorBuilder
import com.mineinabyss.geary.ecs.accessors.building.AccessorBuilderProvider
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.systems.MutableAndSelector
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.query.AndSelector
import com.soywiz.kds.FastIntMap
import com.soywiz.kds.getOrPut
import org.koin.core.component.inject
import kotlin.reflect.KProperty


/**
 * A holder of [Accessor]s that provides logic for reading data off them and calculating their per-archetype cache.
 *
 * @property family A lazily built immutable family that represents all data this holder needs to function.
 */
public open class AccessorHolder : MutableAndSelector(), AccessorBuilderProvider {
    override val engine: Engine by inject()

    private var _family: AndSelector? = null
    public val family: AndSelector by lazy {
        _family ?: error("Tried to accesss family of accessor ${this::class.simpleName}, which was not registered yet.")
    }

    public fun start() {
        onStart()
        _family = build()
    }

    protected open fun onStart() {}

    internal open val accessors = mutableListOf<Accessor<*>>()
    private val perArchetypeCache = FastIntMap<List<List<Any?>>>()

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
//    internal fun iteratorFor(dataScope: RawAccessorDataScope): AccessorCombinationsIterator =
//        AccessorCombinationsIterator(dataScope)

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
    public val isEmpty: Boolean get() = family.and.isEmpty()
}
