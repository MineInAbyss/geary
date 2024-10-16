package com.mineinabyss.geary.engine

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.EntityId
import com.mineinabyss.geary.datatypes.EntityType

interface EntityProvider {
    /** Creates a new entity. */
    fun create(): EntityId
}
