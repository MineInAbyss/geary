package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.api.relations.toRelation
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import com.mineinabyss.geary.ecs.engine.isRelation
import com.mineinabyss.geary.ecs.engine.withRole

public fun GearyType.containsRelationValue(relationValueId: RelationValueId, componentMustHoldData: Boolean = false): Boolean {
    val components = filter { !it.isRelation() }
    return mapNotNull { it.toRelation() }
        .any { relationInType ->
            relationInType.value == relationValueId && components.any {
                if (componentMustHoldData)
                    it == relationInType.key.withRole(HOLDS_DATA)
                else
                    it == relationInType.key
            }
        }
}

public fun GearyType.containsRelationKey(relationKeyId: GearyComponentId): Boolean {
    return any { it.toRelation()?.key == relationKeyId }
}

public operator fun Family.contains(type: GearyType): Boolean = when (this) {
    is AndSelector -> and.all { type in it }
    is AndNotSelector -> andNot.none { type in it }
    is OrSelector -> or.any { type in it }
    is ComponentLeaf -> component in type
    is RelationValueLeaf -> type.containsRelationValue(relationValueId, componentMustHoldData)
    is RelationKeyLeaf -> type.containsRelationKey(relationKeyId)
}
