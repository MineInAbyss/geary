package com.mineinabyss.geary.systems.accessors.types

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.systems.accessors.ArchetypeCacheScope
import com.mineinabyss.geary.systems.accessors.RawAccessorDataScope
import com.mineinabyss.geary.systems.accessors.RelationWithData

public open class RelationWithDataAccessor<K : GearyComponent, V : GearyComponent?>(
    index: Int,
    private val keyIsNullable: Boolean,
    private val relationType: GearyComponentId?,
    private val relationTarget: GearyEntityId?,
) : IndexedAccessor<RelationWithData<K, V>>(index) {
    private fun GearyComponentId.hasData(arc: Archetype) = arc.contains(this.withRole(HOLDS_DATA))

    private val ArchetypeCacheScope.matchedRelations: List<Relation> by cached {
        val relations = when {
            // If we match a specific key and value, we are looking for a specific relation
            relationTarget != null && relationType != null -> listOf(Relation.of(relationType, relationTarget))
            // If we match a specific value, we can have any key
            relationTarget != null -> archetype.relationsByTarget[relationTarget.toLong()]
            // If we match a specific key, we can have any value
            relationType != null -> archetype.relationsByType[relationType.toLong()]
            else -> archetype.relations
        } ?: error("Relation accessor could not find the right relations on a matched archetype.")
        relations
            // Only leave keys with data if K is not nullable
            .run { if (!keyIsNullable) filter { it.type.hasData(archetype) } else this }
    }

    private val ArchetypeCacheScope.valueDataIndices: IntArray
            by cached { matchedRelations.map { archetype.indexOf(it.id) }.toIntArray() }

    private val ArchetypeCacheScope.keyDataIndices: IntArray
            by cached { matchedRelations.map { archetype.indexOf(it.type.withRole(HOLDS_DATA)) }.toIntArray() }

    override fun RawAccessorDataScope.readData(): List<RelationWithData<K, V>> =
        matchedRelations.mapIndexed { i, relation ->
            @Suppress("UNCHECKED_CAST") // Index assignment ensures this should always be true
            (RelationWithData(
                // Key can be nullable but value cannot
                type = archetype.componentData.getOrNull(keyDataIndices[i])?.get(row) as K,
                target = archetype.componentData[valueDataIndices[i]][row] as V,
                typeEntity = relation.type.toGeary(),
                targetEntity = relation.target.toGeary(),
                relation = relation
            ))
        }
}

