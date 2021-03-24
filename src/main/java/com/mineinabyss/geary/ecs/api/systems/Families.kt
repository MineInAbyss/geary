package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.engine.COMP_MASK
import com.mineinabyss.geary.ecs.engine.TRAIT
import com.mineinabyss.geary.ecs.engine.TRAIT_MASK


public fun family(init: FamilyBuilder.() -> Unit): Family = FamilyBuilder().apply(init).build()

public class FamilyBuilder {
    public var match: GearyType = sortedSetOf()
    public var traits: GearyType = sortedSetOf()
    public var andNot: GearyType = sortedSetOf()

    public fun build(): Family {
        return Family(match, traits, andNot)
    }
}

public class Family(
    public val match: GearyType = sortedSetOf(),
    public val traits: GearyType = sortedSetOf(),
    public val andNot: GearyType = sortedSetOf(),
) {
    public operator fun contains(type: GearyType): Boolean =
        match.all { type.hasComponent(it) }
                && traits.all { type.hasTrait(it) }
                && andNot.none { type.hasComponent(it) }

    private fun GearyType.hasTrait(componentId: GearyComponentId) =
        any { typeComponent ->
            typeComponent and TRAIT_MASK == componentId
                    && filter { it and TRAIT == 0uL }.any { component -> typeComponent and COMP_MASK == component and COMP_MASK }
        }

    private fun GearyType.hasComponent(componentId: GearyComponentId) =
        componentId in this
}

public fun traitFor(trait: GearyComponentId, component: GearyComponentId = 0uL): GearyComponentId =
    trait shl 32 or component or TRAIT
