package com.mineinabyss.geary.systems

import com.mineinabyss.geary.context.EngineContext
import com.mineinabyss.geary.context.GearyContextKoin
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.maps.Family2ObjectArrayMap
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.engine.archetypes.ArchetypeEngine
import com.mineinabyss.geary.events.Handler
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.systems.query.GearyQuery

public class QueryManager {
    private val queries = mutableListOf<GearyQuery>()
    private val sourceListeners = mutableListOf<Listener>()
    private val targetListeners = mutableListOf<Listener>()
    private val eventHandlers = mutableListOf<Handler>()

    private val archetypes = Family2ObjectArrayMap<Archetype>()

    public fun init(engine: ArchetypeEngine) {
        registerArchetype(engine.archetypeProvider.rootArchetype)
    }

    public fun trackEventListener(listener: Listener) {
        trackEventListener(
            listener,
            sourceListeners,
            targetListeners,
            archetypes,
            eventHandlers
        )
    }

    public fun trackQuery(query: GearyQuery) {
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
        val matchedHandlers = eventHandlers.filter { archetype.type in it.parentListener.event.family }

        matchedSources.forEach { archetype.addSourceListener(it) }
        matchedTargets.forEach { archetype.addTargetListener(it) }
        matchedHandlers.forEach { archetype.addEventHandler(it) }
        matched.forEach { it.matchedArchetypes += archetype }
    }

    //TODO convert to Sequence
    public fun getEntitiesMatching(family: Family): List<Entity> {
        return archetypes.match(family).flatMap { arc ->
            arc.entities
        }
    }
}
