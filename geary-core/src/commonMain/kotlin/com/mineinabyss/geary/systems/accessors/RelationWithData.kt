package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.Relation
import com.mineinabyss.geary.helpers.toGeary

public data class RelationWithData<K : GearyComponent?, T : GearyComponent?>(
    public val kind: K,
    public val target: T,
    public val relation: Relation,
) {
    public val kindEntity: GearyEntity = relation.kind.toGeary()
    public val targetEntity: GearyEntity= relation.target.toGeary()
}
