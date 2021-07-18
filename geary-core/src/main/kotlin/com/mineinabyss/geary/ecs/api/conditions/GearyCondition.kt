package com.mineinabyss.geary.ecs.api.conditions

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.properties.EntityPropertyHolder
import kotlinx.serialization.Serializable

/**
 * A serializable condition that can be checked against any ECS entity.
 */
@Serializable
public abstract class GearyCondition: EntityPropertyHolder() {
    public fun metFor(entity: GearyEntity): Boolean =
        entity.runWithProperties { check() } ?: false

    protected abstract fun GearyEntity.check(): Boolean
}
