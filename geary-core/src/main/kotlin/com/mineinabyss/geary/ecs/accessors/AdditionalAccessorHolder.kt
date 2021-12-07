package com.mineinabyss.geary.ecs.accessors

public class AdditionalAccessorHolder(
    parentHolder: AccessorHolder
): AccessorHolder() {
    override val accessors: MutableList<Accessor<*>> = parentHolder.accessors.toMutableList()
}
