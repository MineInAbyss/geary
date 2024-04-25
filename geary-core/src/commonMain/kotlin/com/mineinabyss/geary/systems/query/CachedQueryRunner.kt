package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.annotations.optin.ExperimentalGearyApi
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.helpers.fastForEach
import com.mineinabyss.geary.modules.archetypes

class CachedQueryRunner<T : Query> internal constructor(val query: T) {
    val matchedArchetypes: MutableList<Archetype> = mutableListOf()
    val family = query.buildFamily()
    val cachingAccessors = query.cachingAccessors.toTypedArray()

    /**
     * Quickly iterates over all matched entities, running [run] for each.
     *
     * Use [apply] on the query to use its accessors.
     * */
    @OptIn(UnsafeAccessors::class)
    inline fun forEach(crossinline run: T.(T) -> Unit) {
        val matched = matchedArchetypes
        var n = 0
        val size = matched.size // Get size ahead of time to avoid rerunning on entities that end up in new archetypes
        val accessors = cachingAccessors
        while (n < size) {
            val archetype = matched[n]
            archetype.isIterating = true

            // We disallow entity archetype modifications while iterating, but allow creating new entities.
            // These will always end up at the end of the archetype list, so we just don't iterate over them.
            val upTo = archetype.size
            var row = 0
            query.archetype = archetype
            accessors.fastForEach { it.updateCache(archetype) }
            try {
                while (row < upTo) {
                    query.row = row
                    run(query, query)
                    row++
                }
                n++
            } finally {
                archetype.isIterating = false
            }
        }
    }

    /**
     * Allows collecting values as a sequence under some rules. Slower than [forEach] or functions directly on the runner like [map], [any], [find]
     * since an iterator must be used, but can be much faster if terminating early (ex. `take(5)`).
     *
     * ### Rules
     *
     * - Sequence can only be terminated once, and **MUST NOT** be consumed outside the [collector] block.
     * - *Stateful* operations **MUST** run on calculated values, not the query directly, otherwise the same value will be reused.
     *   All of Kotlin's sequence operations tell you if they're *stateful* or *stateless* in their documentation.
     *    - Ex. `map { it.myComponent }.sorted()` is okay, while `sortedBy { it.myComponent }.map { it.myComponent }` is not.
     *      The latter will return the same value for every element.
     * - You **MUST NOT** swap threads, the sequence must run on the sync engine thread or data may be jumbled.
     */
    @ExperimentalGearyApi
    @OptIn(UnsafeAccessors::class)
    fun <R> collect(collector: Sequence<T>.() -> R): R {
        val matched = matchedArchetypes
        var n = 0
        val size = matched.size
        val accessors = cachingAccessors

        // current archetype
        var archetype = archetypes.archetypeProvider.rootArchetype // avoid nullable perf loss
        var upTo = 0

        // current entity
        var row = 0
        query.archetype = archetype
        accessors.fastForEach { it.updateCache(archetype) }

        fun prepareRow(): Boolean {
            if (row >= upTo) return false
            query.row = row
            return true
        }

        fun prepareArchetype(): Boolean {
            if (n >= size) return false
            archetype = matched[n]
            upTo = archetype.size
            query.archetype = archetype
            accessors.fastForEach { it.updateCache(archetype) }
            archetype.isIterating = true
            return true
        }

        fun terminatedError(): Nothing = error("Sequence must be consumed inside use block")
        var closed = false

        val collected = try {
            collector(generateSequence(seedFunction = {
                if (closed) terminatedError()
                prepareArchetype()
                prepareRow()
                query
            }) {
                if (closed) terminatedError()
                row++
                if (prepareRow()) {
                    query
                } else {
                    archetype.isIterating = false
                    n++
                    if (prepareArchetype()) {
                        prepareRow()
                        query
                    } else null
                }
            }.constrainOnce())
        } finally {
            //TODO issues if it's just root archetype?
            archetype.isIterating = false
            closed = true
        }
        return collected
    }

    inline fun <R> map(crossinline run: (T) -> R): List<R> {
        val deferred = mutableListOf<R>()
        forEach { deferred.add(run(it)) }
        return deferred
    }

    inline fun <R> mapNotNull(crossinline run: (T) -> R?): List<R> {
        val deferred = mutableListOf<R>()
        forEach { query -> run(query).let { if (it != null) deferred.add(it) } }
        return deferred
    }

    @PublishedApi
    internal class FoundValue : Throwable()

    inline fun any(crossinline predicate: T.(T) -> Boolean): Boolean {
        try {
            forEach { if (predicate(it)) throw FoundValue() }
        } catch (e: FoundValue) {
            return true
        }

        return false
    }

    inline fun <R> find(crossinline map: T.(T) -> R, crossinline predicate: T.(T) -> Boolean): R? {
        var found: R? = null
        try {
            forEach {
                if (predicate(it)) {
                    found = map(it)
                    throw FoundValue()
                }
            }
        } catch (e: FoundValue) {
            return found
        }

        return found
    }


    data class Deferred<R>(
        val data: R,
        val entity: GearyEntity
    )

    @OptIn(UnsafeAccessors::class)
    inline fun <R> mapWithEntity(crossinline run: T.() -> R): List<Deferred<R>> {
        val deferred = mutableListOf<Deferred<R>>()
        forEach {
            deferred.add(Deferred(run(it), it.unsafeEntity))
        }
        return deferred
    }

    @OptIn(UnsafeAccessors::class)
    fun entities(): List<GearyEntity> {
        val entities = mutableListOf<GearyEntity>()
        forEach { entities.add(it.unsafeEntity) }
        return entities
    }
}

inline fun <R> List<CachedQueryRunner.Deferred<R>>.execOnFinish(run: (data: R, entity: GearyEntity) -> Unit) {
    fastForEach { run(it.data, it.entity) }
}
