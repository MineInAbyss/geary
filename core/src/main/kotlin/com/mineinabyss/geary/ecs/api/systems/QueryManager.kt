package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.geary
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.engine.RELATION
import com.mineinabyss.geary.ecs.query.Family
import com.mineinabyss.geary.ecs.query.Query
import com.mineinabyss.geary.ecs.query.contains
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.onedaybeard.bitvector.BitVector
import net.onedaybeard.bitvector.bitsOf

public object QueryManager {
    private val queries = mutableListOf<Query>()
    private val archetypes = ComponentSparseSet()

    //TODO should be a subclass for trackable queries
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
        return archetypes.match(family).flatMap { arc -> arc.ids.map { geary(it) } }
    }
}
