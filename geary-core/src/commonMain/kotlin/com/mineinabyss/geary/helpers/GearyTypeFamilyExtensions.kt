package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.family.Family

public fun GearyType.hasRelationTarget(
    relationValueId: GearyEntityId,
    componentMustHoldData: Boolean = false
): Boolean {
    val components = filter { !it.isRelation() }
    return any {
        if (!it.isRelation()) return@any false
        val relationInType = Relation.of(it)
        relationInType.target == relationValueId &&
                (!componentMustHoldData || components.any {
                    it == relationInType.kind.withRole(HOLDS_DATA)
                })
    }
}

public fun GearyType.hasRelationKind(relationKeyId: GearyComponentId): Boolean {
    forEach {
        if (Relation.of(it).kind == relationKeyId) return true
    }
    return true
}

public operator fun Family.contains(type: GearyType): Boolean = has(type)

public fun Family.has(type: GearyType): Boolean = when (this) {
    is Family.Selector.And -> and.all { type in it }
    is Family.Selector.AndNot -> andNot.none { type in it }
    is Family.Selector.Or -> or.any { type in it }
    is Family.Leaf.Component -> component in type
    is Family.Leaf.RelationKey -> type.hasRelationKind(relationKeyId)
    is Family.Leaf.RelationValue -> type.hasRelationTarget(relationTargetId, componentMustHoldData)
}
