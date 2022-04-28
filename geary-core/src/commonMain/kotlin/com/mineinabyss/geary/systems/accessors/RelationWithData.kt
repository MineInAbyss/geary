package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.datatypes.GearyEntityId
import com.mineinabyss.geary.datatypes.Relation

public data class RelationWithData<K : GearyComponent?, V : GearyComponent>(
    public val key: K,
    public val value: V,
    public val keyId: GearyEntityId,
    public val valueId: GearyEntityId,
    public val relation: Relation,
)
