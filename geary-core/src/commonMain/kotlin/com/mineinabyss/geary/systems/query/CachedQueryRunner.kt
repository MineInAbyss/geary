package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.helpers.fastForEach

class CachedQueryRunner<T : Query> internal constructor(val query: T) {
    val matchedArchetypes: MutableList<Archetype> = mutableListOf()
    val family = query.buildFamily()
    val cachingAccessors = query.cachingAccessors.toTypedArray()

    /**
     * Quickly iterates over all matched entities, running [run] for each.
     *
     * Use [apply] on the query to use its accessors.
     * */
    inline fun forEach(crossinline run: T.() -> Unit) {
        val matched = matchedArchetypes
        var n = 0
        val size = matched.size // Get size ahead of time to avoid rerunning on entities that end up in new archetypes
        val accessors = cachingAccessors
        while (n < size) {
            val archetype = matched[n]
            archetype.isIterating = true

            // We disallow entity archetype modifications while iterating, but allow creating new entities.
            // These will always end up at the end of the archetype list so we just don't iterate over them.
            val upTo = archetype.size
            var row = 0
            query.originalArchetype = archetype
            accessors.fastForEach { it.updateCache(archetype) }
            while (row < upTo) {
                query.originalRow = row
                run(query)
                row++
            }
            archetype.isIterating = false
            n++
        }
    }

    inline fun <R> map(crossinline run: T.() -> R): List<R> {
        val deferred = mutableListOf<R>()
        forEach { deferred.add(run()) }
        return deferred
    }

    data class Deferred<R>(
        val data: R,
        val entity: GearyEntity
    )

    @OptIn(UnsafeAccessors::class)
    inline fun <R> mapWithEntity(crossinline run: T.() -> R): List<Deferred<R>> {
        val deferred = mutableListOf<Deferred<R>>()
        forEach {
            deferred.add(Deferred(run(), unsafeEntity))
        }
        return deferred
    }

    @OptIn(UnsafeAccessors::class)
    fun entities(): List<GearyEntity> {
        val entities = mutableListOf<GearyEntity>()
        forEach { entities.add(unsafeEntity) }
        return entities
    }
}

inline fun <R> List<CachedQueryRunner.Deferred<R>>.execOnFinish(run: (data: R, entity: GearyEntity) -> Unit) {
    fastForEach { run(it.data, it.entity) }
}
