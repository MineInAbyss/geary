package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.EntityId
import com.mineinabyss.geary.datatypes.Relation
import com.mineinabyss.geary.helpers.toGeary

/**
 * Helper class for getting a compact overview of data stored in a relation.
 */
data class RelationWithData<K : Component?, T : Component?>(
    val data: K,
    val targetData: T,
    val relation: Relation,
) {
    val kind: EntityId = relation.kind
    val target: EntityId = relation.target
}
