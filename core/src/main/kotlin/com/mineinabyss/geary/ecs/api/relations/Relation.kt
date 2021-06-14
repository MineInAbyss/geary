package com.mineinabyss.geary.ecs.api.relations

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.engine.RELATION
import com.mineinabyss.geary.ecs.engine.RELATION_COMPONENT_MASK
import com.mineinabyss.geary.ecs.engine.RELATION_PARENT_MASK

@JvmInline
public value class Relation(
    public val id: GearyComponentId
) : Comparable<Relation> {
    public constructor(
        parent: GearyComponentId,
        component: GearyComponentId = 0uL
    ) : this(
        (parent shl 32 and RELATION_PARENT_MASK)
                or (component and RELATION_COMPONENT_MASK)
                or RELATION
    )

    public val parent: GearyComponentId get() = id and RELATION_PARENT_MASK shr 32
    public val component: GearyComponentId get() = id and RELATION_COMPONENT_MASK and RELATION.inv()

    override fun compareTo(other: Relation): Int = id.compareTo(other.id)

    override fun toString(): String =
        id.toString(2)
}
