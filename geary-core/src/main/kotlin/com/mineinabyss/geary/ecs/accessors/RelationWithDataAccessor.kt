package com.mineinabyss.geary.ecs.accessors

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyEntityId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGearyNoMask
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import com.mineinabyss.geary.ecs.engine.withRole

public open class RelationWithDataAccessor<K : GearyComponent?, V : GearyComponent>(
    index: Int,
    private val keyIsNullable: Boolean,
    private val relationValue: RelationValueId?,
    private val relationKey: GearyComponentId?,
) : Accessor<RelationWithData<K, V>>(index) {
    private fun GearyComponentId.hasData(arc: Archetype) = arc.contains(this.withRole(HOLDS_DATA))

    private val ArchetypeCacheScope.matchedRelations: List<Relation> by cached {
        val relations = when {
            // If we match a specific key and value, we are looking for a specific relation
            //TODO does this need to ensure the archetype has this relation?
            relationValue != null && relationKey != null -> listOf(Relation.of(relationKey, relationValue))
            // If we match a specific value, we can have any key
            relationValue != null -> archetype.relationsByValue[relationValue.id.toLong()]
            // If we match a specific key, we can have any value
            relationKey != null -> archetype.relationsByKey[relationKey.toLong()]
            else -> archetype.relations
        }
        relations!!
            // Only leave keys with data if K is not nullable
            .run { if (!keyIsNullable) filter { it.key.hasData(archetype) } else this }
    }

    private val ArchetypeCacheScope.valueDataIndices: IntArray
            by cached { matchedRelations.map { archetype.indexOf(it.id) }.toIntArray() }

    private val ArchetypeCacheScope.keyDataIndices: IntArray
            by cached { matchedRelations.map { archetype.indexOf(it.key.withRole(HOLDS_DATA)) }.toIntArray() }

    override fun RawAccessorDataScope.readData(): List<RelationWithData<K, V>> =
        matchedRelations.mapIndexed { i, relation ->
            RelationWithData(
                key = archetype.componentData[keyDataIndices[i]][row] as K,
                value = archetype.componentData[valueDataIndices[i]][row] as V,
                //TODO is this ever useful?
                relation = relation.value.id.toGearyNoMask(),
                keyId = relation.key
            )
        }
}

public class RelationWithData<K : GearyComponent?, V : GearyComponent>(
    public val key: K,
    public val value: V,
    public val relation: GearyEntity,
    public val keyId: GearyEntityId
)
