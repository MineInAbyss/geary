package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.engine.isRelation

public operator fun GearyType.contains(relation: Relation): Boolean {
    val components = filter { !it.isRelation() }
    return this
        .filter { it.isRelation() }
        .any { item ->
            val relationInType = Relation(item)
            relationInType.parent == relation.parent
                    && components.any { it == relationInType.component }
        }
}

public operator fun Family.contains(type: GearyType): Boolean = when (this) {
    is AndSelector -> and.all { type in it }
    is AndNotSelector -> andNot.none { type in it }
    is OrSelector -> or.any { type in it }
    is ComponentLeaf -> component in type
    is RelationLeaf -> relation in type
}
