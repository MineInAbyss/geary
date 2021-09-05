package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.query.Family
import com.mineinabyss.geary.ecs.query.Query
import com.mineinabyss.geary.ecs.query.contains

public object QueryManager {
    private val queries = mutableListOf<Query>()
    private val archetypes = ComponentSparseSet()

    public fun trackQuery(query: Query) {
        query.matchedArchetypes += archetypes.match(query.family)
        queries.add(query)
    }

    internal fun registerArchetype(archetype: Archetype) {
        archetypes.add(archetype, archetype.type)
        queries.filter { archetype.type in it.family }.forEach {
            it.matchedArchetypes += archetype
        }
    }

    public fun getEntitiesMatching(family: Family): List<GearyEntity> {
        return archetypes.match(family).flatMap { arc -> arc.ids.map { it.toGeary() } }
    }
}
