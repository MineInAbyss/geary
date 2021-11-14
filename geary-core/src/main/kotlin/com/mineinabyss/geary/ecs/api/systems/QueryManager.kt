package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.query.Family
import com.mineinabyss.geary.ecs.query.Query
import com.mineinabyss.geary.ecs.query.contains

public object QueryManager {
    private val queries = mutableListOf<Query>()
    private val eventListeners = mutableListOf<GearyListener>()

    private val archetypes = object : Component2ObjectArrayMap<Archetype>() {
        override fun Archetype.getGearyType() = type
    }

    public fun trackEventListener(listener: GearyListener) {
        val matched = archetypes.match(listener.family)
        eventListeners += listener
        for (archetype in matched) {
            listener.apply { GearyHandlerScope(archetype, this).init() }
        }
    }

    public fun trackQuery(query: Query) {
        val matched = archetypes.match(query.family)
        query.matchedArchetypes += matched
        queries.add(query)
    }

    internal fun registerArchetype(archetype: Archetype) {
        archetypes.add(archetype, archetype.type)
        val matched = queries.filter { archetype.type in it.family }
        val matchedListeners = eventListeners.filter { archetype.type in it.family }
        matchedListeners.forEach { it.apply { GearyHandlerScope(archetype, this).init() } }
        matched.forEach { it.matchedArchetypes += archetype }
    }

    //TODO convert to Sequence
    public fun getEntitiesMatching(family: Family): List<GearyEntity> {
        return archetypes.match(family).flatMap { arc -> arc.ids.map { it.toGeary() } }
    }
}
