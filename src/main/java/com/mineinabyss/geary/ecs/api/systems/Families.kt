package com.mineinabyss.geary.ecs.api.systems


public fun family(init: FamilyBuilder.() -> Unit): Family = FamilyBuilder().apply(init).build()

public class FamilyBuilder {
    private var match: List<Accessor<*>> = listOf()
    private var andNot: List<Accessor<*>> = listOf()

    public fun match(vararg accessors: Accessor<*>) {
        match = accessors.toList()
    }

    public fun andNot(vararg accessors: Accessor<*>) {
        andNot = accessors.toList()
    }

    public fun build(): Family {
        return Family(match, andNot)
    }
}

public class Family(
    public val match: List<Accessor<*>> = listOf(),
    public val andNot: List<Accessor<*>> = listOf(),
)
