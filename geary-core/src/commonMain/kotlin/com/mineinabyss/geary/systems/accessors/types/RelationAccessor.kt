package com.mineinabyss.geary.systems.accessors.types

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.systems.accessors.ArchetypeCacheScope
import com.mineinabyss.geary.systems.accessors.RawAccessorDataScope
import com.mineinabyss.geary.systems.accessors.RelationWithData

public open class RelationWithDataAccessor<K : GearyComponent?, T : GearyComponent?>(
    index: Int,
    private val kind: GearyComponentId,
    private val target: GearyEntityId,
) : IndexedAccessor<RelationWithData<K, T>>(index) {
    private val anyComponentId = componentId<Any>()
    private val ArchetypeCacheScope.matchedRelations: List<Relation> by cached {
        val specificKind = kind and ENTITY_MASK != anyComponentId
        val specificTarget = target and ENTITY_MASK != anyComponentId
        val relations = when {
            specificKind && specificTarget -> listOf(Relation.of(kind, target))
            specificTarget -> archetype.relationsByTarget[target.toLong()]?.run {
                if (kind.hasRole(HOLDS_DATA)) filter { it.hasRole(HOLDS_DATA) } else this
            }
            specificKind -> archetype.relationsByKind[kind.toLong()]
            else -> archetype.relations
        } ?: error("Relation accessor could not find the right relations on a matched archetype.")
        relations
            // Target holds data means there must be a component of type target with data on this entity
            .run { if (target.holdsData()) filter { it.target.withRole(HOLDS_DATA) in archetype.type } else this }
    }

    private val ArchetypeCacheScope.relationDataIndices: IntArray
            by cached { matchedRelations.map { archetype.indexOf(it.id) }.toIntArray() }

    private val ArchetypeCacheScope.targetDataIndices: IntArray
            by cached { matchedRelations.map { archetype.indexOf(it.target.withRole(HOLDS_DATA)) }.toIntArray() }


    override fun RawAccessorDataScope.readData(): List<RelationWithData<K, T>> =
        matchedRelations.mapIndexed { i, relation ->
            @Suppress("UNCHECKED_CAST") // Index assignment ensures this should always be true
            (RelationWithData(
                kind = archetype.componentData.getOrNull(relationDataIndices[i])?.get(row) as K,
                target = archetype.componentData.getOrNull(targetDataIndices[i])?.get(row) as T,
                relation = relation
            ))
        }
}

