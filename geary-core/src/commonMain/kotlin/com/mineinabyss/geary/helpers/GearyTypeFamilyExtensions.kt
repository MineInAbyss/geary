package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.family.Family

public fun GearyType.hasRelationTarget(
    relationTargetId: GearyEntityId,
    componentMustHoldData: Boolean = false
): Boolean {
    val components = filter { !it.isRelation() }
    return any { comp ->
        if (!comp.isRelation()) return@any false
        val relationInType = Relation.of(comp)
        relationInType.target == relationTargetId &&
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
    is Family.Leaf.RelationKind -> type.hasRelationKind(relationKindId)
    is Family.Leaf.RelationTarget -> type.hasRelationTarget(relationTargetId, componentMustHoldData)
    else -> TODO("Kotlin compiler is shitting itself")
}
