package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.Relation

public data class RelationWithData<K : GearyComponent?, T : GearyComponent?>(
    public val kind: K,
    public val target: T,
    public val kindEntity: GearyEntity,
    public val targetEntity: GearyEntity,
    public val relation: Relation,
)
