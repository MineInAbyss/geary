package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.family.Family

public fun GearyType.hasRelationTarget(
    target: GearyEntityId,
    kindMustHoldData: Boolean = false
): Boolean = any { comp ->
    if (!comp.isRelation()) return@any false
    val relationInType = Relation.of(comp)
    relationInType.target == target &&
            (!kindMustHoldData || contains(relationInType.kind.withRole(HOLDS_DATA)))
}

public fun GearyType.hasRelationKind(
    kind: GearyComponentId,
    targetMustHoldData: Boolean = false
): Boolean =
    any {
        it.isRelation() && with(Relation.of(it)) {
            this.kind == kind && (!targetMustHoldData ||)
        }
    }

public operator fun Family.contains(type: GearyType): Boolean = has(type)

public fun Family.has(type: GearyType): Boolean = when (this) {
    is Family.Selector.And -> and.all { type in it }
    is Family.Selector.AndNot -> andNot.none { type in it }
    is Family.Selector.Or -> or.any { type in it }
    is Family.Leaf.Component -> component in type
    is Family.Leaf.KindToAny -> type.hasRelationKind(kind, targetMustHoldData)
    is Family.Leaf.AnyToTarget -> type.hasRelationTarget(target, kindMustHoldData)
    else -> TODO("Kotlin compiler is shitting itself")
}
