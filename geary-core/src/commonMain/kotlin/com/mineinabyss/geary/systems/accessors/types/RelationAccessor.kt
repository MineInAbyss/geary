package com.mineinabyss.geary.systems.accessors.types

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.systems.accessors.ArchetypeCacheScope
import com.mineinabyss.geary.systems.accessors.RawAccessorDataScope
import com.mineinabyss.geary.systems.accessors.RelationWithData

public open class RelationWithDataAccessor<K : Component?, T : Component?>(
    index: Int,
    private val kind: ComponentId,
    private val target: EntityId,
) : IndexedAccessor<RelationWithData<K, T>>(index) {
    private val ArchetypeCacheScope.matchedRelations: List<Relation> by cached {
        archetype.getRelations(kind, target)
    }

    private val ArchetypeCacheScope.relationDataIndices: IntArray
            by cached { matchedRelations.map { archetype.indexOf(it.id) }.toIntArray() }

    private val ArchetypeCacheScope.targetDataIndices: IntArray
            by cached { matchedRelations.map { archetype.indexOf(it.target.withRole(HOLDS_DATA)) }.toIntArray() }


    override fun RawAccessorDataScope.readData(): List<RelationWithData<K, T>> =
        matchedRelations.mapIndexed { i, relation ->
            @Suppress("UNCHECKED_CAST") // Index assignment ensures this should always be true
            (RelationWithData(
                data = archetype.componentData.getOrNull(relationDataIndices[i])?.get(row) as K,
                targetData = archetype.componentData.getOrNull(targetDataIndices[i])?.get(row) as T,
                relation = relation
            ))
        }
}

