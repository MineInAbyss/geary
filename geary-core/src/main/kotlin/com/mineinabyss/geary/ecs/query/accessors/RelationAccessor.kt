package com.mineinabyss.geary.ecs.query.accessors

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGearyNoMask
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationParent
import com.mineinabyss.geary.ecs.engine.iteration.accessors.ArchetypeCache
import com.mineinabyss.geary.ecs.engine.iteration.accessors.RawAccessorDataScope

public open class RelationAccessor<T : GearyComponent>(
    index: Int,
    private val relationParent: RelationParent,
) : Accessor<RelationData<T>>(index) {
    private val ArchetypeCache.matchedRelations: List<Relation>
            by cached { archetype.relations[relationParent.id.toLong()]!! }

    private val ArchetypeCache.dataIndices: IntArray
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
