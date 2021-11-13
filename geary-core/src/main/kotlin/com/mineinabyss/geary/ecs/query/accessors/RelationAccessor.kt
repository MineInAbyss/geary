package com.mineinabyss.geary.ecs.query.accessors

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGearyNoMask
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationParent
import com.mineinabyss.geary.ecs.engine.iteration.ArchetypeIterator
import com.mineinabyss.geary.ecs.engine.iteration.accessors.AccessorDataScope

public open class RelationAccessor<T : GearyComponent>(
    index: Int,
    private val relationParent: RelationParent,
) : Accessor<RelationData<T>>(index) {
    private val ArchetypeIterator.matchedRelations: List<Relation>
            by cached { archetype.relations[relationParent.id.toLong()]!! }

    private val ArchetypeIterator.dataIndices: IntArray
            by cached { matchedRelations.map { archetype.indexOf(it.id) }.toIntArray() }

    override fun AccessorDataScope.readData(): List<RelationData<T>> =
        iterator.matchedRelations.mapIndexed { i, relation ->
            RelationData(
                parentData = archetype.componentData[iterator.dataIndices[i]][row] as T,
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
