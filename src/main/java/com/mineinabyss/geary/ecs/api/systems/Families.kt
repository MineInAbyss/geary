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
    public operator fun contains(type: GearyType): Boolean =
        match.all { type.hasComponent(it) }
                && relations.all { type.hasRelation(it) }
                && andNot.none { type.hasComponent(it) }

    private fun GearyType.hasRelation(relation: Relation): Boolean {
        val components = filter { it and RELATION == 0uL }
        return any { item ->
            Relation(item).parent == relation.parent
                    && components.any { it == relation.component }
        }
    }

    private fun GearyType.hasComponent(componentId: GearyComponentId) =
        componentId in this
}

