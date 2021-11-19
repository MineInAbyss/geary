package com.mineinabyss.geary.ecs.api.conditions

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import kotlinx.serialization.Serializable

/**
 * A serializable condition that can be checked against any ECS entity.
 */
@Serializable
public abstract class GearyCondition : (GearyEntity) -> Boolean {
    public override fun invoke(entity: GearyEntity): Boolean = entity.check()

    protected abstract fun GearyEntity.check(): Boolean
}
