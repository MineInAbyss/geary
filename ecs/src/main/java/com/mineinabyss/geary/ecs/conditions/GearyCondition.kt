package com.mineinabyss.geary.ecs.conditions

import com.mineinabyss.geary.ecs.GearyEntity

/**
 * A serializable condition that can be checked against a certain entity.
 *
 * @param components Components the entity must have.
 * @param player Additional conditions relating to the player associated with this entity.
 */
public interface GearyCondition {
    public fun conditionsMet(entity: GearyEntity): Boolean
}
