package com.mineinabyss.geary.ecs.api.relations

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import com.mineinabyss.geary.ecs.engine.RELATION
import com.mineinabyss.geary.ecs.engine.RELATION_COMPONENT_MASK
import com.mineinabyss.geary.ecs.engine.RELATION_PARENT_MASK

public inline class Relation(
    public val id: GearyComponentId
) {
    public constructor(
        parent: GearyComponentId,
        component: GearyComponentId
    ) : this((parent shl 32 and RELATION_PARENT_MASK) or (component and RELATION_COMPONENT_MASK))

    public val parent: GearyComponentId get() = id and RELATION_PARENT_MASK shr 32 or HOLDS_DATA or RELATION
    public val component: GearyComponentId get() = id and RELATION_COMPONENT_MASK and RELATION.inv()
}

