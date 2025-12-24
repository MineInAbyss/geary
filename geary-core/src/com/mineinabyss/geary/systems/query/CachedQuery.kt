package com.mineinabyss.geary.systems.query

import androidx.collection.MutableObjectList
import androidx.collection.mutableLongListOf
import com.mineinabyss.geary.annotations.optin.ExperimentalGearyApi
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.EntityArray
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.toEntityArray
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.engine.archetypes.ArchetypeProvider
import com.mineinabyss.geary.helpers.fastForEach
import com.mineinabyss.geary.modules.WorldScoped
import org.koin.core.component.get


context(row: Int, world: WorldScoped) val row get() = row
//context(row: Int, world: WorldScoped) val unsafeEntity get() = row
class CachedQuery<T : Query> internal constructor(val query: T) : AutoCloseable {
    val matchedArchetypes: MutableObjectList<Archetype> = MutableObjectList()
    val family = query.family

    var closed: Boolean = false
        internal set

    /**
     * Quickly iterates over all matched archetypes, calling [block] for each.
     */
    inline fun forEachArchetype(block: Archetype.(T) -> Unit) {
        ensureNotClosed()
        val matched = matchedArchetypes
        var n = 0
        val size = matched.size
        val query = query
        while (n < size) {
            val archetype = matched[n]
            if (archetype.size != 0) block(archetype, query)
            n++
        }
    }

    /**
     * Quickly iterates over all matched entities, running [run] for each.
     *
     * Use [apply] on the query to use its accessors.
     * */
    @OptIn(UnsafeAccessors::class)
    inline fun forEach(run: context(Int) (T) -> Unit) {
        forEachArchetype { query ->
            // We disallow entity archetype modifications while iterating, but allow creating new entities.
            // These will always end up at the end of the archetype list, so we just don't iterate over them.
            query.load(this)
            forEachRow {
                run(query)
            }
        }
    }

    @OptIn(UnsafeAccessors::class)
    inline fun forEachAccessor(run: context(Int) (T) -> Unit) {

    }

    /**
     * Runs this query on a list of entities gathered ahead of time.
     *
     * ### Unsafe constraints
     * - Ensure all [entities] match this query.
     * - Ensure [entities] belong to the same world as the query.
     * - When iterating, ensure of all entities matching this query, only the current entity is modified.
     */
    @UnsafeAccessors
    inline fun forEachMutating(entities: EntityArray, run: context(Int) (Entity, T) -> Unit) {
        val query = query
        val world = query.world
        require(entities.world == query.world) { "Entities must belong to the same world as the query" }
        val records = world.records
        entities.forEachId { id ->
            records.runOn(id) { archetype, row ->
                query.load(archetype)
                run(row, Entity(id, world), query)
            }
        }
    }

    /**
     * Matches entities ahead of time and iterates over the matched list, allowing for archetype modifications.
     *
     * ### Unsafe constraints
     * - When iterating, ensure of all entities matching this query, only the current entity is modified.
     */
    @UnsafeAccessors
    inline fun forEachMutating(run: context(Int) (Entity, T) -> Unit) {
        ensureNotClosed()
        forEachMutating(entities(), run)
    }

    fun Archetype.getData() = componentData

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
        ensureNotClosed()
        val matched = matchedArchetypes
        var n = 0
        val size = matched.size

        // current archetype
        var archetype = query.world.get<ArchetypeProvider>().rootArchetype // avoid nullable perf loss
        var upTo = 0

        // current entity
        var row = 0
        query.load(archetype)

        TODO()
//        fun prepareRow(): Boolean {
//            if (row >= upTo) return false
//            query.row = row
//            return true
//        }
//
//        fun prepareArchetype(): Boolean {
//            if (n >= size) return false
//            archetype = matched[n]
//            upTo = archetype.size
//            query.archetype = archetype
//            accessors.fastForEach { it.load(archetype) }
//            return true
//        }

//        fun terminatedError(): Nothing = error("Sequence must be consumed inside use block")
//        var closed = false
//
//        val collected = try {
//            collector(generateSequence(seedFunction = {
//                if (closed) terminatedError()
//                prepareArchetype()
//                prepareRow()
//                query
//            }) {
//                if (closed) terminatedError()
//                row++
//                if (prepareRow()) {
//                    query
//                } else {
//                    n++
//                    if (prepareArchetype()) {
//                        prepareRow()
//                        query
//                    } else null
//                }
//            }.constrainOnce())
//        } finally {
//            //TODO issues if it's just root archetype?
//            closed = true
//        }
//        return collected
    }

    inline fun <R> map(crossinline run: (T) -> R): List<R> {
        ensureNotClosed()
        val deferred = mutableListOf<R>()
        forEach { deferred.add(run(it)) }
        return deferred
    }

    inline fun <R> mapNotNull(crossinline run: (T) -> R?): List<R> {
        ensureNotClosed()
        val deferred = mutableListOf<R>()
        forEach { query -> run(query).let { if (it != null) deferred.add(it) } }
        return deferred
    }

    @OptIn(UnsafeAccessors::class)
    inline fun <R> mapWithEntity(crossinline run: context(Int) (T) -> R): List<Deferred<R>> {
        ensureNotClosed()
        val deferred = mutableListOf<Deferred<R>>()
        forEach {
            // TODO use EntityList instead
            deferred.add(Deferred(run(it), Entity(it.unsafeEntity, it.world)))
        }
        return deferred
    }

    @OptIn(UnsafeAccessors::class)
    inline fun <R> mapNotNullWithEntity(crossinline run: (T) -> R?): List<Deferred<R>> {
        ensureNotClosed()
        val deferred = mutableListOf<Deferred<R>>()
        forEach { query ->
            run(query).let { if (it != null) deferred.add(Deferred(it, Entity(query.unsafeEntity, query.world))) }
        }
        return deferred
    }

    inline fun any(crossinline predicate: (T) -> Boolean): Boolean {
        ensureNotClosed()
        try {
            forEach { if (predicate(it)) throw FoundValue() }
        } catch (e: FoundValue) {
            return true
        }

        return false
    }

    inline fun <R> find(crossinline map: (T) -> R, crossinline predicate: (T) -> Boolean): R? {
        ensureNotClosed()
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

    fun count(): Int {
        ensureNotClosed()
        var count = 0
        forEach { count++ }
        return count
    }

    @OptIn(UnsafeAccessors::class)
    inline fun filter(crossinline predicate: (T) -> Boolean): EntityArray {
        ensureNotClosed()
        val deferred = mutableLongListOf()
        forEach { if (predicate(it)) deferred.add(it.unsafeEntity.toLong()) }
        return deferred.toEntityArray(query.world)
    }

    @OptIn(UnsafeAccessors::class)
    fun entities(): EntityArray {
        ensureNotClosed()
        val entities = mutableLongListOf()
        forEach { entities.add(it.unsafeEntity.toLong()) }
        return entities.toEntityArray(query.world)
    }

    data class Deferred<R>(
        val data: R,
        val entity: GearyEntity,
    )

    @PublishedApi
    internal inline fun ensureNotClosed() {
        !closed || error("Query is closed")
    }

    /**
     * Stops tracking matched entities for this query.
     */
    override fun close() {
        query.world.queryManager.untrackQuery(this)
    }

    @PublishedApi
    internal class FoundValue : Throwable()
}

inline fun <R> List<CachedQuery.Deferred<R>>.execOnFinish(run: (data: R, entity: GearyEntity) -> Unit) {
    fastForEach { run(it.data, it.entity) }
}
