package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.SerializableGearyComponent

/**
 * A component that holds a list of components to be persisted.
 *
 * It's not persisted itself, as we know which components are persistent when deserializing an entity by looking
 * at the ones that were serialized to it.
 */
public class PersistingComponents(
    public val persisting: MutableSet<SerializableGearyComponent> = mutableSetOf()
) : MutableSet<SerializableGearyComponent> by persisting
