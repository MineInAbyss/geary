package com.mineinabyss.geary.ecs.accessors

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGearyNoMask
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationParent

public open class RelationAccessor<T : GearyComponent>(
    index: Int,
    private val relationParent: RelationParent,
) : Accessor<RelationData<T>>(index) {
    private val ArchetypeCacheScope.matchedRelations: List<Relation>
            by cached { archetype.relations[relationParent.id.toLong()]!! }

    private val ArchetypeCacheScope.dataIndices: IntArray
            by cached { matchedRelations.map { archetype.indexOf(it.id) }.toIntArray() }

    override fun RawAccessorDataScope.readData(): List<RelationData<T>> =
        matchedRelations.mapIndexed { i, relation ->
            RelationData(
                parentData = archetype.componentData[dataIndices[i]][row] as T,
                relation = relationParent.id.toGearyNoMask(),
                component = relation.component.toGearyNoMask()
            )
        }
}

public class RelationData<T : GearyComponent>(
    public val parentData: T,
    public val relation: GearyEntity,
    public val component: GearyEntity
)
