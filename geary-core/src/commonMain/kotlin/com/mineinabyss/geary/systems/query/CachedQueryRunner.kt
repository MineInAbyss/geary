package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.helpers.fastForEach
import com.mineinabyss.geary.systems.accessors.type.ComponentAccessor

class CachedQueryRunner<T : Query> internal constructor(val query: T) {
    val matchedArchetypes: MutableList<Archetype> = mutableListOf()
    val family = query.buildFamily()
    val accessors = query.accessors.toTypedArray().filterIsInstance<ComponentAccessor<*>>()

    inline fun <R> toList(crossinline map: T.() -> R): List<R> {
        val list = mutableListOf<R>()
        forEach { list.add(this.map()) }
        return list
    }

    /**
     * Quickly iterates over all matched entities, running [run] for each.
     *
     * Use [apply] on the query to use its accessors.
     * */
    inline fun forEach(crossinline run: T.() -> Unit) {
        val matched = matchedArchetypes
        var n = 0
        val size = matched.size // Get size ahead of time to avoid rerunning on entities that end up in new archetypes
        val accessors = accessors
        while (n < size) {
            val archetype = matched[n]
            archetype.isIterating = true
            val upTo = archetype.size
            var row = 0
            query.originalArchetype = archetype
            // TODO upTo isn't perfect for cases where entities may be added or removed in the same iteration
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
        forEach {
            deferred.add(run())
        }
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
            deferred.add(Deferred(run(), archetype.getEntity(row)))
        }
        return deferred
    }
}

inline fun <R> List<CachedQueryRunner.Deferred<R>>.execOnFinish(run: (data: R, entity: GearyEntity) -> Unit) {
    fastForEach { run(it.data, it.entity) }
}
