package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.Relation
import com.mineinabyss.geary.helpers.toGeary

public data class RelationWithData<K : GearyComponent?, T : GearyComponent?>(
    public val data: K,
    public val targetData: T,
    public val relation: Relation,
) {
    public val kind: GearyEntity = relation.kind.toGeary()
    public val target: GearyEntity = relation.target.toGeary()
}
