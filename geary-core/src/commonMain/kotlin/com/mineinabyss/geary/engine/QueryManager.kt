package com.mineinabyss.geary.engine

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.events.queries.Observer
import com.mineinabyss.geary.systems.query.CachedQueryRunner
import com.mineinabyss.geary.systems.query.Query

interface QueryManager {
    fun <T: Query> trackQuery(query: T): CachedQueryRunner<T>

    /** Returns a list of entities matching the given family. */
    fun getEntitiesMatching(family: Family): List<Entity>

    /**
     * Returns a sequence of entities matching the given family.
     * This should be faster than [getEntitiesMatching] but will depend on impl.
     * In an archetypal engine, this gets all matching archetypes first, then maps them to entities as a sequence.
     */
    fun getEntitiesMatchingAsSequence(family: Family): Sequence<Entity>
}
