package com.mineinabyss.geary.ecs.accessors.building

import com.mineinabyss.geary.ecs.accessors.Accessor
import com.mineinabyss.geary.ecs.accessors.AccessorHolder

/**
 * A builder that can provide an accessor for [AccessorHolder]s.
 *
 * @see Accessor
 */
public fun interface AccessorBuilder<T : Accessor<*>> {
    /** Provides an [Accessor] to a [holder] and [index] this accessor should be placed in for that holder. */
    public fun build(holder: AccessorHolder, index: Int): T
}
