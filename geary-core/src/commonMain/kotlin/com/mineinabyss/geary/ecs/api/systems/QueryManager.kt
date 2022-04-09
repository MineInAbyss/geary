package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.engine.EngineContext
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.events.handlers.GearyHandler
import com.mineinabyss.geary.ecs.helpers.GearyContextKoin
import com.mineinabyss.geary.ecs.query.Family
import com.mineinabyss.geary.ecs.query.Query
import com.mineinabyss.geary.ecs.query.contains

internal expect fun trackEventListener(
    listener: GearyListener,
    sourceListeners: MutableList<GearyListener>,
    targetListeners: MutableList<GearyListener>,
    archetypes: Component2ObjectArrayMap<Archetype>,
    eventHandlers: MutableList<GearyHandler>,
)

public class QueryManager : EngineContext by GearyContextKoin() {
    private val queries = mutableListOf<Query>()
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

    public fun trackQuery(query: Query) {
        query.start()
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
