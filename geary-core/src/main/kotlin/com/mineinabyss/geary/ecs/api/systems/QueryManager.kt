package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.query.Family
import com.mineinabyss.geary.ecs.query.Query
import com.mineinabyss.geary.ecs.query.contains

public object QueryManager {
    private val queries = mutableListOf<Query>()
    private val componentAddSystems = mutableListOf<ComponentAddSystem>()

    private val archetypes = object : Component2ObjectArrayMap<Archetype>() {
        override fun Archetype.getGearyType() = type
    }

    public fun trackComponentAddSystem(system: ComponentAddSystem) {
        val matched = archetypes.match(system.family)
        componentAddSystems += system
        matched.forEach { it.addEventListener(system) }
    }

    public fun trackQuery(query: Query) {
        val matched = archetypes.match(query.family)
        query.matchedArchetypes += matched
        queries.add(query)
    }

    internal fun registerArchetype(archetype: Archetype) {
        archetypes.add(archetype, archetype.type)
        val matched = queries.filter { archetype.type in it.family }
        val matchedEvents = componentAddSystems.filter { archetype.type in it.family }
        matchedEvents.forEach { archetype.addEventListener(it) }
        matched.forEach { it.matchedArchetypes += archetype }
    }

    //TODO convert to Sequence
    public fun getEntitiesMatching(family: Family): List<GearyEntity> {
        return archetypes.match(family).flatMap { arc -> arc.ids.map { it.toGeary() } }
    }
}
