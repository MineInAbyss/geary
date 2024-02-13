package com.mineinabyss.geary.engine

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.query.GearyQuery

interface QueryManager {
    fun trackEventListener(listener: Listener)
    fun trackQuery(query: GearyQuery)

    /** Returns a list of entities matching the given family. */
    fun getEntitiesMatching(family: Family): List<Entity>

    /**
     * Returns a sequence of entities matching the given family.
     * This should be faster than [getEntitiesMatching] but will depend on impl.
     * In an archetypal engine, this gets all matching archetypes first, then maps them to entities as a sequence.
     */
    fun getEntitiesMatchingAsSequence(family: Family): Sequence<Entity>
}
