package com.mineinabyss.geary.ecs.api.conditions

import com.mineinabyss.geary.ecs.api.entities.GearyEntity

/**
 * A serializable condition that can be checked against any ECS entity.
 */
public interface GearyCondition {
    public fun conditionsMet(entity: GearyEntity): Boolean
}
