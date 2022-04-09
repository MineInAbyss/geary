package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.engine.holdsData

public sealed class Family

public class ComponentLeaf(
    public val component: GearyComponentId
) : Family()

public class RelationKeyLeaf(
    public val relationKeyId: GearyComponentId
) : Family()

public class RelationValueLeaf(
    public val relationValueId: RelationValueId,
    public val componentMustHoldData: Boolean = false
) : Family()

public class AndSelector(
    public val and: List<Family>
) : Family() {
    //TODO support getting these beyond just the top-level.
    // (part of a bigger rewrite to how branching family declarations are done)
    public val components: List<GearyComponentId>
            by lazy { and.filterIsInstance<ComponentLeaf>().map { it.component } }

    public val componentsWithData: List<GearyComponentId>
            by lazy { components.filter { it.holdsData() } }

    public val relationValueTypes: List<RelationValueId>
            by lazy { and.filterIsInstance<RelationValueLeaf>().map { it.relationValueId } }
}

public class AndNotSelector(
    public val andNot: List<Family>
) : Family()

public class OrSelector(
    public val or: List<Family>
) : Family()
