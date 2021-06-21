package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.geary
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.engine.RELATION
import com.mineinabyss.geary.ecs.query.Query
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.onedaybeard.bitvector.BitVector
import net.onedaybeard.bitvector.bitsOf


private typealias ComponentMap<T> = Long2ObjectOpenHashMap<T>

internal class ComponentSparseSet<T> {
    internal val elements: MutableList<T> = mutableListOf()
    internal val componentMap = ComponentMap<BitVector>()

    fun add(element: T, family: Family) {
        elements += element
        val index = elements.lastIndex
        (family.match + family.relations.mapTo(mutableSetOf()) { it.parent or RELATION }).forEach { id ->
            componentMap.getOrPut(id.toLong()) { bitsOf() }.set(index)
        }
    }

    fun match(family: Family): List<T> {
        if (family.match.isEmpty() && family.relations.isEmpty()) return listOf()
        //copy one component's bitset and go through the others, keeping only bits present in all sets
        val allowed = (family.match + family.relations.mapTo(mutableSetOf()) { it.parent or RELATION })
            //if a set isn't present, there's 0 matches
            .mapTo(mutableListOf()) { (componentMap[it.toLong()] ?: return listOf()) }
            //only copy the first bitset, all further operations only mutate it
            .also { list -> list[0] = list[0].copy() }
            .reduce { a, b -> a.and(b).let { a } }

        //remove any bits present in any bitsets from andNot
        val matchingBits =
            if (family.andNot.isEmpty())
                allowed
            else family.andNot
                .mapNotNull { (componentMap[it.toLong()]) }
                .fold(allowed) { a, b -> a.andNot(b).let { a } }

        val matchingArchetypes = mutableListOf<T>()
        matchingBits.forEachBit { matchingArchetypes += elements[it] }
        return matchingArchetypes
    }
}

public object SystemManager {
    internal val queries = mutableListOf<Query>()
    internal val archetypes = ComponentSparseSet<Archetype>()

    //TODO should be a subclass for trackable queries
    public fun trackQuery(query: Query) {
        query.matchedArchetypes += archetypes.match(query.family)
        queries.add(query)
    }

    internal fun registerArchetype(archetype: Archetype) {
        val family = Family.of(archetype.type)
        archetypes.add(archetype, family)
        queries.filter { archetype.type in it.family }.forEach {
            it.matchedArchetypes += archetype
        }
    }

    public fun getEntitiesMatching(family: Family): List<GearyEntity> {
        return archetypes.match(family).flatMap { arc -> arc.ids.map { geary(it) } }
    }
}
