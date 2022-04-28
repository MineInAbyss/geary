package com.mineinabyss.geary.systems

import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.context.EngineContext
import com.mineinabyss.geary.context.GearyContextKoin
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.maps.Component2ObjectArrayMap
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.events.GearyHandler
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.systems.query.GearyQuery

public class QueryManager : EngineContext by GearyContextKoin() {
    private val queries = mutableListOf<GearyQuery>()
    private val sourceListeners = mutableListOf<GearyListener>()
    private val targetListeners = mutableListOf<GearyListener>()
    private val eventHandlers = mutableListOf<GearyHandler>()

    private val archetypes = Component2ObjectArrayMap<Archetype>()

    public fun init() {
        registerArchetype(engine.rootArchetype)
    }

    public fun trackEventListener(listener: GearyListener) {
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
    public fun getEntitiesMatching(family: Family): List<GearyEntity> {
        return archetypes.match(family).flatMap { arc ->
            arc.cleanup() //TODO async safety
            arc.ids.map { it.toGeary() }
        }
    }
}

public interface QueryContext {
    public val queryManager: QueryManager
}
