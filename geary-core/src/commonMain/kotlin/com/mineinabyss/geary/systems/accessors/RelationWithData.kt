package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.Relation
import com.mineinabyss.geary.helpers.toGeary

data class RelationWithData<K : Component?, T : Component?>(
    val data: K,
    val targetData: T,
    val relation: Relation,
) {
    val kind: Entity = relation.kind.toGeary()
    val target: Entity = relation.target.toGeary()
}
