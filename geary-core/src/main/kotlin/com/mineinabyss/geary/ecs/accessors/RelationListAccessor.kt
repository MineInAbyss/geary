package com.mineinabyss.geary.ecs.accessors

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.entities.toGearyNoMask
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationDataType

public open class RelationListAccessor<T : GearyComponent>(
    index: Int,
    private val relationDataType: RelationDataType,
) : Accessor<List<RelationWithData<T>>>(index) {
    private val ArchetypeCacheScope.matchedRelations: List<Relation>
            by cached { archetype.relations[relationDataType.id.toLong()]!! }

    //TODO reuse code from RelationWithDataAccessor
    private val ArchetypeCacheScope.parentDataIndices: IntArray
            by cached { matchedRelations.map { archetype.indexOf(it.id) }.toIntArray() }

    private val ArchetypeCacheScope.componentDataIndices: IntArray
            by cached { matchedRelations.map { archetype.indexOf(it.key) }.toIntArray() }

    override fun RawAccessorDataScope.readData(): List<List<RelationWithData<T>>> =
        listOf(matchedRelations.mapIndexed { i, relation ->
            RelationWithData(
                parentData = archetype.componentData[parentDataIndices[i]][row] as T,
                componentData = archetype.componentData[componentDataIndices[i]][row],
                relation = relationDataType.id.toGearyNoMask(),
                component = relation.key.toGearyNoMask()
            )
        })
}
