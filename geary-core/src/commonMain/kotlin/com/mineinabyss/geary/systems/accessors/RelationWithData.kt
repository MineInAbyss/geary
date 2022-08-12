package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.Relation
import com.mineinabyss.geary.helpers.toGeary

public data class RelationWithData<K : Component?, T : Component?>(
    public val data: K,
    public val targetData: T,
    public val relation: Relation,
) {
    public val kind: Entity = relation.kind.toGeary()
    public val target: Entity = relation.target.toGeary()
}
