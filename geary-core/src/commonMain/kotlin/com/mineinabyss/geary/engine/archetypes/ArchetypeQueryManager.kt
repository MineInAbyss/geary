package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.maps.Family2ObjectArrayMap
import com.mineinabyss.geary.engine.QueryManager
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.helpers.fastForEach
import com.mineinabyss.geary.systems.query.CachedQuery
import com.mineinabyss.geary.systems.query.Query
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

class ArchetypeQueryManager : QueryManager {
    private val queries = mutableListOf<CachedQuery<*>>()

    private val archetypes = Family2ObjectArrayMap<Archetype>(
        getIndex = { it.id },
        setIndex = { it, index -> it.id = index }
    )

    private val archetypeRegistryLock = SynchronizedObject()

    val archetypeCount get() = archetypes.elements.size

    override fun <T : Query> trackQuery(query: T): CachedQuery<T> {
        query.initialize()
        val queryRunner = CachedQuery(query)
        val matched = archetypes.match(queryRunner.family)
        queryRunner.matchedArchetypes += matched
        queries.add(queryRunner)
        return queryRunner
    }

    internal fun registerArchetype(archetype: Archetype) = synchronized(archetypeRegistryLock) {
        archetypes.add(archetype, archetype.type)
        val matched = queries.filter { archetype.type in it.family }
        matched.fastForEach { it.matchedArchetypes += archetype }
    }

    internal fun unregisterArchetype(archetype: Archetype) = synchronized(archetypeRegistryLock) {
        archetypes.remove(archetype)
        val matched = queries.filter { archetype.type in it.family }
        matched.fastForEach { it.matchedArchetypes -= archetype }
    }

    fun getArchetypesMatching(family: Family): List<Archetype> {
        return archetypes.match(family)
    }

    override fun getEntitiesMatching(family: Family): List<Entity> {
        return getArchetypesMatching(family).flatMap(Archetype::entities)
    }

    override fun getEntitiesMatchingAsSequence(family: Family): Sequence<Entity> {
        return getArchetypesMatching(family)
            .asSequence()
            .flatMap(Archetype::entities)
    }
}
