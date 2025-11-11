package com.mineinabyss.geary.engine

import com.mineinabyss.geary.datatypes.EntityId
import com.mineinabyss.geary.datatypes.EntityIdArray
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.systems.query.CachedQuery
import com.mineinabyss.geary.systems.query.Query

interface QueryManager {
    /**
     * Tracks a query by caching the archetypes that match it as they are registered.
     */
    fun <T : Query> trackQuery(query: T): CachedQuery<T>

    /**
     * Stops matching new archetypes to a query, marks it as closed to prevent further use.
     *
     * @return Whether the query was untracked.
     */
    fun <T : Query> untrackQuery(query: CachedQuery<T>): Boolean

    /** Returns a list of entities matching the given family. */
    fun getEntitiesMatching(family: Family): EntityIdArray

    /**
     * Returns a sequence of entities matching the given family.
     * This should be faster than [getEntitiesMatching] but will depend on impl.
     * In an archetypal engine, this gets all matching archetypes first, then maps them to entities as a sequence.
     */
    fun getEntitiesMatchingAsSequence(family: Family): Sequence<EntityId>

    fun childrenOf(parent: EntityId): EntityIdArray
}
