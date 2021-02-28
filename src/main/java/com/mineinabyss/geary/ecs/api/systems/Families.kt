package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType


public fun family(init: FamilyBuilder.() -> Unit): Family = FamilyBuilder().apply(init).build()

public class FamilyBuilder {
    private var match: List<GearyComponentId> = listOf()
    private var andNot: List<GearyComponentId> = listOf()

    public fun match(vararg accessors: GearyComponentId) {
        match = accessors.toList()
    }

    public fun andNot(vararg accessors: GearyComponentId) {
        andNot = accessors.toList()
    }

    public fun build(): Family {
        return Family(match, andNot)
    }
}

public class Family(
    public val match: List<GearyComponentId> = listOf(),
    public val andNot: List<GearyComponentId> = listOf(),
) {
    public val type: GearyType = match.sorted()

    public operator fun contains(type: GearyType): Boolean =
        type.containsAll(match) && andNot.none { type.contains(it) }
}
