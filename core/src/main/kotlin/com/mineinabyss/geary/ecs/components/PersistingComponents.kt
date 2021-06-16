package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.api.GearyComponent

/**
 * A component that holds a list of components to be persisted.
 *
 * It's not persisted itself, as we know which components are persistent when deserializing an entity by looking
 * at the ones that were serialized to it.
 */
private data class PersistingComponents(
    public val components: MutableSet<GearyComponent> = mutableSetOf(),
    public var hashed: Int = components.hashCode(),
) : MutableSet<GearyComponent> by components {
    public fun updateComponentHash(): Int {
        val newHashed = components.hashCode()
        hashed = newHashed
        return newHashed
    }
}
