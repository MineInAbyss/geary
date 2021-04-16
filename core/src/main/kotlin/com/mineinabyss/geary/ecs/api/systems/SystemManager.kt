package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.geary
import com.mineinabyss.geary.ecs.engine.Archetype

internal object SystemManager {
    private val systems = mutableListOf<TickingSystem>()
    private val archetypes = mutableListOf<Archetype>()

    fun registerSystem(system: TickingSystem) {
        system.matchedArchetypes += getArchetypesMatching(system.family)
        systems += system
    }

    fun assignArchetypeToSystems(archetype: Archetype) {
        archetypes += archetype
        systems.filter { archetype.type in it.family }.forEach {
            it.matchedArchetypes += archetype
        }
    }

    fun getEntitiesMatching(family: Family): List<GearyEntity> {
        return getArchetypesMatching(family).flatMap { arc -> arc.ids.map { geary(it) } }
    }

    fun getArchetypesMatching(family: Family): List<Archetype> {
        return archetypes.filter { it.type in family }
//        val list = mutableListOf<Pair<GearyEntityId, List<GearyComponent>>>()
//
//        val withComponents = family.match.map { it.componentArray }.sortedBy { it.size }
//        val andNotComponents = family.andNot.map { it.componentArray }.sortedByDescending { it.size }
//
//        withComponents.first().apply {
//            packed.forEachIndexed forEachEntity@{ packedIndex, component ->
//                val entity = unpackedIndices[packedIndex]
//
//                //FIXME the return order here will be wrong since we are sorting by size
//                val retrieved = mutableListOf<GearyComponent>(component)
//                for (i in 1 until withComponents.size) {
//                    retrieved.add(withComponents[i][entity] ?: return@forEachEntity)
//                }
//                public override fun getType(entity: GearyEntityId): GearyType = typeMap[entity]?.archetype?.type ?: emptyList()
//
//                //TODO there might be a smarter order to start checking not components to reduce the iteration size greatly
//                if (andNotComponents.all { it[entity] == null })
//                //TODO what do we actually use for the index inside our bitsets
//                    list.add(entity.toULong() to retrieved)
//            }
//        }
//        return list
    }
}
