package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.engine.RELATION
import java.util.*

public class Family(
    public val match: GearyType = sortedSetOf(),
    public val relations: SortedSet<Relation> = sortedSetOf(),
    public val andNot: GearyType = sortedSetOf(),
) {
    public companion object {
        public fun of(type: GearyType): Family = Family(
            match = GearyType(type.filter { it and RELATION == 0uL }),
            relations = type.filter { it and RELATION != 0uL }.map { Relation(it) }.toSortedSet()
        )
    }

    public operator fun contains(type: GearyType): Boolean =
        match.all { type.hasComponent(it) }
                && relations.all { type.hasRelation(it) }
                && andNot.none { type.hasComponent(it) }

    private fun GearyType.hasRelation(relation: Relation): Boolean {
        val components = filter { it and RELATION == 0uL }
        return this
            .filter { it and RELATION != 0uL }
            .any { item ->
                val relationInType = Relation(item)
                relationInType.parent == relation.parent
                        && components.any { it == relationInType.component }
            }
    }

    private fun GearyType.hasComponent(componentId: GearyComponentId) =
        componentId in this
}

