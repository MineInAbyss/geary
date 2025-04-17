package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.family.Family

fun EntityType.hasRelationTarget(
    target: EntityId,
    kindMustHoldData: Boolean = false
): Boolean = any {
    it.isRelation() && Relation.of(it).run {
        this.target == target && (!kindMustHoldData || contains(this.kind.withRole(HOLDS_DATA)))
    }
}

fun EntityType.hasRelationKind(
    kind: ComponentId,
    targetMustHoldData: Boolean = false
): Boolean = any {
    it.isRelation() && Relation.of(it).run {
        this.kind == kind && (!targetMustHoldData || contains(this.target.withRole(HOLDS_DATA)))
    }
}

operator fun Family.contains(type: EntityType): Boolean = has(type)

fun Family.has(type: EntityType): Boolean = when (this) {
    is Family.Selector.And -> and.all { type in it }
    is Family.Selector.AndNot -> andNot.none { type in it }
    is Family.Selector.Or -> or.any { type in it }
    is Family.Leaf.Component -> component in type
    is Family.Leaf.KindToAny -> type.hasRelationKind(kind, targetMustHoldData)
    is Family.Leaf.AnyToTarget -> type.hasRelationTarget(target, kindMustHoldData)
}
