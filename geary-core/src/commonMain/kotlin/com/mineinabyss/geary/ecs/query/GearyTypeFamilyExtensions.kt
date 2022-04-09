package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import com.mineinabyss.geary.ecs.engine.isRelation
import com.mineinabyss.geary.ecs.engine.withRole

public fun GearyType.containsRelationValue(
    relationValueId: RelationValueId,
    componentMustHoldData: Boolean = false
): Boolean {
    val components = filter { !it.isRelation() }
    return any {
        if (!it.isRelation()) return@any false
        val relationInType = Relation.of(it)
        relationInType.value == relationValueId &&
                (!componentMustHoldData || components.any {
                    it == relationInType.key.withRole(HOLDS_DATA)
                })
    }
}

public fun GearyType.containsRelationKey(relationKeyId: GearyComponentId): Boolean {
    forEach {
        if (Relation.of(it).key == relationKeyId) return true
    }
    return true
}

public operator fun Family.contains(type: GearyType): Boolean = when (this) {
    is AndSelector -> and.all { type in it }
    is AndNotSelector -> andNot.none { type in it }
    is OrSelector -> or.any { type in it }
    is ComponentLeaf -> component in type
    is RelationValueLeaf -> type.containsRelationValue(relationValueId, componentMustHoldData)
    is RelationKeyLeaf -> type.containsRelationKey(relationKeyId)
}
