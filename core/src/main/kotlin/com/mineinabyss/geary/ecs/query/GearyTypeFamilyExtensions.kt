package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.relations.RelationParent
import com.mineinabyss.geary.ecs.engine.isRelation
import com.mineinabyss.geary.ecs.api.relations.toRelation

public operator fun GearyType.contains(relationParent: RelationParent): Boolean {
    val components = filter { !it.isRelation() }
    return mapNotNull { it.toRelation() }
        .any { relationInType ->
            relationInType.parent == relationParent
                    && components.any { it == relationInType.component }
        }
}

public operator fun Family.contains(type: GearyType): Boolean = when (this) {
    is AndSelector -> and.all { type in it }
    is AndNotSelector -> andNot.none { type in it }
    is OrSelector -> or.any { type in it }
    is ComponentLeaf -> component in type
    is RelationLeaf -> relationParent in type
}
