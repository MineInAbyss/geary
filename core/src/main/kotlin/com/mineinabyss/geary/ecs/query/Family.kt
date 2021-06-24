package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.relations.Relation

public sealed class Family

public class ComponentLeaf(
    public val component: GearyComponentId
) : Family()

public class RelationLeaf(
    public val relation: Relation
) : Family()


public class AndSelector(
    public val and: List<Family>
) : Family()

public class AndNotSelector(
    public val andNot: List<Family>
) : Family()

public class OrSelector(
    public val or: List<Family>
) : Family()
