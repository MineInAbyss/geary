package com.mineinabyss.geary.ecs.accessors

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGearyNoMask
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationDataType
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import com.mineinabyss.geary.ecs.engine.withRole

public open class RelationWithDataAccessor<T : GearyComponent>(
    index: Int,
    private val relationDataType: RelationDataType,
) : Accessor<RelationWithData<T>>(index) {
    private val ArchetypeCacheScope.matchedRelations: List<Relation> by cached {
        archetype.relations[relationDataType.id.toLong()]!!
            .filter { archetype.contains(it.key.withRole(HOLDS_DATA)) }
    }

    private val ArchetypeCacheScope.parentDataIndices: IntArray
            by cached { matchedRelations.map { archetype.indexOf(it.id) }.toIntArray() }

    private val ArchetypeCacheScope.componentDataIndices: IntArray
            by cached { matchedRelations.map { archetype.indexOf(it.key.withRole(HOLDS_DATA)) }.toIntArray() }

    override fun RawAccessorDataScope.readData(): List<RelationWithData<T>> =
        matchedRelations.mapIndexed { i, relation ->
            RelationWithData(
                parentData = archetype.componentData[parentDataIndices[i]][row] as T,
                componentData = archetype.componentData[componentDataIndices[i]][row],
                relation = relationDataType.id.toGearyNoMask(),
                component = relation.key.toGearyNoMask()
            )
        }
}

public data class RelationWithData<T : GearyComponent>(
    public val parentData: T,
    public val componentData: Any,
    public val relation: GearyEntity,
    public val component: GearyEntity
)
