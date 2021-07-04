package com.mineinabyss.geary.ecs.query.accessors

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.gearyNoMask
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationParent
import com.mineinabyss.geary.ecs.engine.iteration.AccessorData
import com.mineinabyss.geary.ecs.engine.iteration.ArchetypeIterator
import com.mineinabyss.geary.ecs.query.Query
/*
public open class RelationListAccessor<T : GearyComponent>(
    query: Query,
    private val relationParent: RelationParent,
) : Accessor<List<RelationData<T>>>(query) {
    private val relationIndex: Int = query.relationParents.lastIndex

    private val ArchetypeIterator.matchedRelations: List<Relation>
            by cached { archetype.relations[relationParent.id.toLong()]!! }

    private val ArchetypeIterator.dataIndices: IntArray
            by cached { matchedRelations.map { archetype.indexOf(it.id) }.toIntArray() }

    override fun AccessorData.readData(): List<RelationData<T>> =
        iterator.matchedRelations.mapIndexed { i, relation ->
            RelationData(
                parentData = archetype.componentData[iterator.dataIndices[i]][row] as T,
                relation = gearyNoMask(relationParent.id),
                component = gearyNoMask(relation.component)
            )
        }
}

public class RelationData<T : GearyComponent>(
    public val parentData: T,
    public val relation: GearyEntity,
    public val component: GearyEntity
)*/
