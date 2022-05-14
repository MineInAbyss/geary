package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.Relation

public data class RelationWithData<K : GearyComponent?, V : GearyComponent?>(
    public val type: K,
    public val target: V,
    public val typeEntity: GearyEntity,
    public val targetEntity: GearyEntity,
    public val relation: Relation,
)
