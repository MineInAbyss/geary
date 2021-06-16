package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.relations.Relation

public class FamilyBuilder {
    public var match: MutableList<GearyComponentId> = mutableListOf()
    public var relations: MutableList<Relation> = mutableListOf()
    public var andNot: MutableList<GearyComponentId> = mutableListOf()

    public fun build(): Family = Family(
        match = GearyType(match),
        relations = relations.toSortedSet(),
        andNot = GearyType(andNot)
    )
}
