package com.mineinabyss.geary.engine

import com.mineinabyss.geary.datatypes.*

public interface EntityProvider {
    public fun newEntity(initialComponents: Collection<Component>): Entity

    public fun removeEntity(entity: Entity)
}
