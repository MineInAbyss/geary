package com.mineinabyss.geary.engine.archetypes

import co.touchlab.stately.concurrency.Synchronizable
import co.touchlab.stately.concurrency.synchronize
import com.mineinabyss.geary.components.ReservedComponents
import com.mineinabyss.geary.datatypes.EntityId
import com.mineinabyss.geary.datatypes.EntityIdArray
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.datatypes.maps.Family2ObjectArrayMap
import com.mineinabyss.geary.engine.QueryManager
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.helpers.fastForEach
import com.mineinabyss.geary.systems.query.CachedQuery
import com.mineinabyss.geary.systems.query.Query

class ArchetypeQueryManager : QueryManager {
    private val queries = mutableListOf<CachedQuery<*>>()

    private val archetypes = Family2ObjectArrayMap<Archetype>(
        getIndex = { it.id },
        setIndex = { it, index -> it.id = index }
    )

    private val archetypeRegistryLock = Synchronizable()

    val archetypeCount get() = archetypes.elements.size

    override fun <T : Query> trackQuery(query: T): CachedQuery<T> {
        query.initialize()
        val queryRunner = CachedQuery(query)
        val matched = archetypes.match(queryRunner.family)
        queryRunner.matchedArchetypes += matched
        queries.add(queryRunner)
        return queryRunner
    }

    internal fun registerArchetype(archetype: Archetype) = archetypeRegistryLock.synchronize {
        archetypes.add(archetype, archetype.type)
        val matched = queries.filter { archetype.type in it.family }
        matched.fastForEach { it.matchedArchetypes += archetype }
    }

    internal fun unregisterArchetype(archetype: Archetype) = archetypeRegistryLock.synchronize {
        archetypes.remove(archetype)
        val matched = queries.filter { archetype.type in it.family }
        matched.fastForEach { it.matchedArchetypes -= archetype }
    }

    fun getArchetypesMatching(family: Family): List<Archetype> {
        return archetypes.match(family)
    }

    override fun getEntitiesMatching(family: Family): EntityIdArray {
        val archetypes = getArchetypesMatching(family)
        // TODO avoid the list creation here, make the ULongArray directly
        return archetypes.flatMap(Archetype::entities).toULongArray()
    }

    override fun getEntitiesMatchingAsSequence(family: Family): Sequence<EntityId> {
        return getArchetypesMatching(family)
            .asSequence()
            .flatMap(Archetype::entities)
    }

    override fun childrenOf(parent: EntityId): EntityIdArray {
        return getEntitiesMatching(family {
            hasRelation(ReservedComponents.CHILD_OF, parent)
        })
    }
}
