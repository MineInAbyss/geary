package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.maps.Family2ObjectArrayMap
import com.mineinabyss.geary.engine.QueryManager
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.systems.query.CachedQueryRunner
import com.mineinabyss.geary.systems.query.Query
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

typealias Listener = com.mineinabyss.geary.systems.Listener<*>

class ArchetypeQueryManager : QueryManager {
    private val queries = mutableListOf<CachedQueryRunner<*>>()
    private val sourceListeners = mutableListOf<Listener>()
    private val targetListeners = mutableListOf<Listener>()
    private val eventListeners = mutableListOf<Listener>()

    private val archetypes = Family2ObjectArrayMap<Archetype>(
        getIndex = { it.id },
        setIndex = { it, index -> it.id = index }
    )

    private val archetypeRegistryLock = SynchronizedObject()

    val archetypeCount get() = archetypes.elements.size

    override fun trackEventListener(listener: Listener) {
        if (listener.event.and.isNotEmpty()) {
            val eventFamilyMatch = archetypes.match(listener.event)
            for (archetype in eventFamilyMatch) archetype.eventListeners += listener
            eventListeners.add(listener)
        }

        // Only start tracking a listener for the parts it actually cares for
        if (listener.source.and.isNotEmpty()) {
            val sourcesMatched = archetypes.match(listener.source)
            for (archetype in sourcesMatched) archetype.sourceListeners += listener
            sourceListeners.add(listener)
        }
        if (listener.target.and.isNotEmpty()) {
            val targetsMatched = archetypes.match(listener.target)
            for (archetype in targetsMatched) archetype.targetListeners += listener
            targetListeners.add(listener)
        }
    }

    override fun <T : Query> trackQuery(query: T): CachedQueryRunner<T> {
        val queryRunner = CachedQueryRunner(query)
        val matched = archetypes.match(queryRunner.family)
        queryRunner.matchedArchetypes += matched
        queries.add(queryRunner)
        return queryRunner
    }

    internal fun registerArchetype(archetype: Archetype) = synchronized(archetypeRegistryLock) {
        archetypes.add(archetype, archetype.type)

        val (matched, matchedSources, matchedTargets, matchedEvents) = getQueriesMatching(archetype)

        matchedSources.forEach { archetype.sourceListeners += it }
        matchedTargets.forEach { archetype.targetListeners += it }
        matchedEvents.forEach { archetype.eventListeners += it }
        matched.forEach { it.matchedArchetypes += archetype }
    }

    internal fun unregisterArchetype(archetype: Archetype) = synchronized(archetypeRegistryLock) {
        archetypes.remove(archetype)
        val matched = queries.filter { archetype.type in it.family }
        matched.forEach { it.matchedArchetypes -= archetype }
    }

    data class MatchedQueries(
        val queries: List<CachedQueryRunner<*>>,
        val sourceListeners: List<Listener>,
        val targetListeners: List<Listener>,
        val eventListeners: List<Listener>
    )

    fun getQueriesMatching(archetype: Archetype): MatchedQueries {
        val matched = queries.filter { archetype.type in it.family }
        val matchedSources = sourceListeners.filter { archetype.type in it.source }
        val matchedTargets = targetListeners.filter { archetype.type in it.target }
        val matchedEvents = eventListeners.filter { archetype.type in it.event }
        return MatchedQueries(matched, matchedSources, matchedTargets, matchedEvents)
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
