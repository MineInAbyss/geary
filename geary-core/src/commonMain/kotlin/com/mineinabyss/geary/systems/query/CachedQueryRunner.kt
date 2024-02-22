package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.datatypes.RecordPointer
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.helpers.fastForEachWithIndex

class CachedQueryRunner<T : Query> internal constructor(val query: T) {
    val matchedArchetypes: MutableList<Archetype> = mutableListOf()
    val family = query.target.buildFamily()

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
        while (n < size) {
            val archetype = matched[n]
            archetype.isIterating = true
            val upTo = archetype.size
            val target = query.target
            // TODO upTo isn't perfect for cases where entities may be added or removed in the same iteration
            target.originalArchetype = archetype

            for (entityIndex in 0 until upTo) {
                target.delegated = false
                target.originalRow = entityIndex
                run(query)
            }
            archetype.isIterating = false
            n++
        }
    }
}
