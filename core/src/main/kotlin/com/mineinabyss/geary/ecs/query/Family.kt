package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationParent

public sealed class Family

public class ComponentLeaf(
    public val component: GearyComponentId
) : Family()

public class RelationLeaf(
    public val relationParent: RelationParent
) : Family()


public class AndSelector(
    public val and: List<Family>
) : Family() {
    //TODO support getting these beyond just the top-level.
    // (part of a bigger rewrite to how branching family declarations are done)
    public val match: GearyType get() = GearyType(and.filterIsInstance<ComponentLeaf>().map { it.component })
    public val relationParents: List<RelationParent> get() = and.filterIsInstance<RelationLeaf>().map { it.relationParent }
}

public class AndNotSelector(
    public val andNot: List<Family>
) : Family()

public class OrSelector(
    public val or: List<Family>
) : Family()
