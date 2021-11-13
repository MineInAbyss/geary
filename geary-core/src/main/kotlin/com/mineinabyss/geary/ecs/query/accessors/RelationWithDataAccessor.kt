package com.mineinabyss.geary.ecs.query.accessors

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGearyNoMask
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationParent
import com.mineinabyss.geary.ecs.engine.holdsData
import com.mineinabyss.geary.ecs.engine.iteration.AccessorDataScope
import com.mineinabyss.geary.ecs.engine.iteration.ArchetypeIterator

public open class RelationWithDataAccessor<T : GearyComponent>(
    index: Int,
    private val relationParent: RelationParent,
) : Accessor<RelationWithData<T>>(index) {
    private val ArchetypeIterator.matchedRelations: List<Relation> by cached {
        archetype.relations[relationParent.id.toLong()]!!
            .filter { it.component.holdsData() || archetype.contains(it.component) }
    }

    private val ArchetypeIterator.parentDataIndices: IntArray
            by cached { matchedRelations.map { archetype.indexOf(it.id) }.toIntArray() }

    private val ArchetypeIterator.componentDataIndices: IntArray
            by cached { matchedRelations.map { archetype.indexOf(it.component) }.toIntArray() }

    override fun AccessorDataScope.readData(): List<RelationWithData<T>> =
        iterator.matchedRelations.mapIndexed { i, relation ->
            RelationWithData(
                parentData = archetype.componentData[iterator.parentDataIndices[i]][row] as T,
                componentData = archetype.componentData[iterator.componentDataIndices[i]][row],
                relation = relationParent.id.toGearyNoMask(),
                component = relation.component.toGearyNoMask()
            )
        }
}

public data class RelationWithData<T : GearyComponent>(
    public val parentData: T,
    public val componentData: Any,
    public val relation: GearyEntity,
    public val component: GearyEntity
)
