package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType


public fun family(init: FamilyBuilder.() -> Unit): Family = FamilyBuilder().apply(init).build()

public class FamilyBuilder {
    public var match: GearyType = sortedSetOf()
    public var andNot: GearyType = sortedSetOf()

    public fun match(vararg accessors: GearyComponentId) {
        match = GearyType(accessors)
    }

    public fun andNot(vararg accessors: GearyComponentId) {
        andNot = GearyType(accessors)
    }

    public fun build(): Family {
        return Family(match, andNot)
    }
}

public class Family(
    public val match: GearyType = sortedSetOf(),
    public val andNot: GearyType = sortedSetOf(),
) {
    public val type: GearyType = match

    public operator fun contains(type: GearyType): Boolean =
        type.containsAll(match) && andNot.none { type.contains(it) }
}
