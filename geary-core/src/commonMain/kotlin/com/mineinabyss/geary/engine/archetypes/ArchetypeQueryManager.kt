package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.maps.Family2ObjectArrayMap
import com.mineinabyss.geary.engine.QueryManager
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.query.GearyQuery

class ArchetypeQueryManager : QueryManager {
    private val queries = mutableListOf<GearyQuery>()
    private val sourceListeners = mutableListOf<Listener>()
    private val targetListeners = mutableListOf<Listener>()
    private val eventListeners = mutableListOf<Listener>()

    private val archetypes = Family2ObjectArrayMap<Archetype>()

    override fun trackEventListener(listener: Listener) {
        val eventFamilyMatch = archetypes.match(listener.event.family)
        for (archetype in eventFamilyMatch) archetype.addEventHandler(listener)
        eventListeners.add(listener)

        // Only start tracking a listener for the parts it actually cares for
        if (!listener.source.isEmpty) {
            val sourcesMatched = archetypes.match(listener.source.family)
            for (archetype in sourcesMatched) archetype.addSourceListener(listener)
            sourceListeners.add(listener)
        }
        if (!listener.target.isEmpty) {
            val targetsMatched = archetypes.match(listener.target.family)
            for (archetype in targetsMatched) archetype.addSourceListener(listener)
            targetListeners.add(listener)
        }
    }

    override fun trackQuery(query: GearyQuery) {
        val matched = archetypes.match(query.family)
        query.matchedArchetypes += matched
        queries.add(query)
        query.registered = true
    }

    internal fun registerArchetype(archetype: Archetype) {
        archetypes.add(archetype, archetype.type)

        val matched = queries.filter { archetype.type in it.family }
        val matchedSources = sourceListeners.filter { archetype.type in it.source.family }
        val matchedTargets = targetListeners.filter { archetype.type in it.target.family }
        val matchedEvents = eventListeners.filter { archetype.type in it.event.family }

        matchedSources.forEach { archetype.addSourceListener(it) }
        matchedTargets.forEach { archetype.addTargetListener(it) }
        matchedEvents.forEach { archetype.addEventHandler(it) }
        matched.forEach { it.matchedArchetypes += archetype }
    }

    //TODO convert to Sequence
    override fun getEntitiesMatching(family: Family): List<Entity> {
        return archetypes.match(family).flatMap { arc ->
            arc.entities
        }
    }
}
